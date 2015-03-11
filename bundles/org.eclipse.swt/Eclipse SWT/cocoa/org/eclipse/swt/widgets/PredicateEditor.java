package org.eclipse.swt.widgets;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.keyvaluecoding.*;
import org.eclipse.swt.keyvaluecoding.cocoa.*;
import org.eclipse.swt.predicate.*;
import org.eclipse.swt.predicateeditor.*;
import org.eclipse.swt.widgets.Composite;

public class PredicateEditor extends Control implements PredicateVisitable {
    
    public enum PredicateOperatorType {
        NSLessThanPredicateOperatorType,
        NSLessThanOrEqualToPredicateOperatorType,
        NSGreaterThanPredicateOperatorType,
        NSGreaterThanOrEqualToPredicateOperatorType,
        NSEqualToPredicateOperatorType,
        NSNotEqualToPredicateOperatorType,
        NSMatchesPredicateOperatorType,
        NSLikePredicateOperatorType,
        NSBeginsWithPredicateOperatorType,
        NSEndsWithPredicateOperatorType,
        NSInPredicateOperatorType,
        NSCustomSelectorPredicateOperatorType, 
        NSContainsPredicateOperatorType,  // OSX 10.5 and later
        NSBetweenPredicateOperatorType;   // OSX 10.5 and later

        public int value() {
            return this.ordinal();
        }
    }
    
    public enum CompoundPredicateType {
        NSNotPredicateType,
        NSAndPredicateType,
        NSOrPredicateType;
        
        public int value() {
            return this.ordinal();
        }
     }
        
    public enum AttributeType {
        NSUndefinedAttributeType(0),
        NSInteger16AttributeType(100),
        NSInteger32AttributeType(200),
        NSInteger64AttributeType(300),
        NSDecimalAttributeType(400),
        NSDoubleAttributeType(500),
        NSFloatAttributeType(600),
        NSStringAttributeType(700),
        NSBooleanAttributeType(800),
        NSDateAttributeType(900),
        NSBinaryDataAttributeType(1000),
        NSTransformableAttributeType(1800),
        NSObjectIDAttributeType(2000);
        
        private int value;
        
        private AttributeType(int value) {
            this.value = value;
        }
        
        public int value() {
            return value;
        }
    }
    
    public enum ComparisonPredicateModifier {
        NSDirectPredicateModifier,
        NSAllPredicateModifier,
        NSAnyPredicateModifier;
        
        public int value() {
            return this.ordinal();
        }
    }
    
    public enum ComparisonPredicateOption {
        NSCaseInsensitivePredicateOption(0x01),
        NSDiacriticInsensitivePredicateOption(0x02),
        NSNormalizedPredicateOption(0x04),
        NSLocaleSensitivePredicateOption(0x08);
        
        private int value;
        
        private ComparisonPredicateOption(int value) {
            this.value = value;
        }
        
        public int value() {
            return value;
        }
    }
    
    public enum AutoresizingMaskOptions {
        NSViewNotSizable(0),
        NSViewMinXMargin(1),
        NSViewWidthSizable(2),
        NSViewMaxXMargin(4),
        NSViewMinYMargin(8),
        NSViewHeightSizable(16),
        NSViewMaxYMargin(32);
        
        private int value;
        
        private AutoresizingMaskOptions(int value) {
            this.value = value;
        }
        
        public int value() {
            return value;
        }
    }
    
    class PredicateEditorNotification implements KeyValueCoding {
        
        public void setValueForKey(Object value, String key) {
            currentPredicate = new NSPredicate((id)value);
            
            firstTimePredicateUpdate = false;
                              
            if (enabledNotifications)               
                sendSelectionEvent (SWT.Selection);
            
            handlePredicateChanged();
        }

        public Object valueForKey(String key) {
            return currentPredicate;
        }
    }
    
    private NSPredicateEditor nsPredicateEditor;
    
    boolean firstTimePredicateUpdate = true;
    NSPredicate currentPredicate;
    protected List<NSPredicateEditorRowTemplate> rowTemplates = new ArrayList<NSPredicateEditorRowTemplate>();
    protected HashSet<DynamicRightValuesRowTemplate> dynamicRowTemplateInstances = new HashSet<DynamicRightValuesRowTemplate>();
    protected HashSet<String> dynamicRightValuesRowsKeyPaths = new HashSet<String>();
    
    protected HashSet<MoneyRowTemplate> moneyRowTemplateInstances = new HashSet<MoneyRowTemplate>();
    protected HashSet<LengthRowTemplate> lengthRowTemplateInstances = new HashSet<LengthRowTemplate>();
    protected HashSet<WeightRowTemplate> weightRowTemplateInstances = new HashSet<WeightRowTemplate>();
    
    private boolean enabledNotifications = false;
    protected PredicateEditorNotification notification = new PredicateEditorNotification();
    SWTKeyValueCodingDecorator kvNotification;
    private long lastNumberOfRows;
    
    public PredicateEditor(Composite parent, int style) {
        super(parent, style);
        
        kvNotification = new SWTKeyValueCodingDecorator(notification);
        bind("value", kvNotification, "value");
    }
    
    public void addSelectionListener(SelectionListener listener) {
        checkWidget ();
        
        if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
        
        TypedListener typedListener = new TypedListener(listener);
        addListener (SWT.Selection,typedListener);
        addListener (SWT.DefaultSelection,typedListener);
        
        enableNotifications();
    }
    
    public void enableNotifications() {
        this.enabledNotifications = true;
    }
    
    public void disableNotifications() {
        this.enabledNotifications = false;
    }
    
    public void removeSelectionListener(SelectionListener listener) {
        checkWidget ();
        
        if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
        
        if (eventTable == null) return;
        
        eventTable.unhook (SWT.Selection, listener);
        eventTable.unhook (SWT.DefaultSelection, listener);
    }
    
    public void setRowTemplates(List<NSPredicateEditorRowTemplate> rowTemplates) {                
        NSMutableArray templates = NSMutableArray.arrayWithCapacity(rowTemplates.size());
        for (NSPredicateEditorRowTemplate template : rowTemplates)
            templates.addObject(template);
        
        nsPredicateEditor.setRowTemplates(templates);
        
        this.rowTemplates = rowTemplates;
    }
    
    public void setRowTemplates() {
        setRowTemplates(rowTemplates);
    }
    
    public void addRowTemplate(PredicateEditorRowTemplate template) {
        rowTemplates.add(new NSPredicateEditorRowTemplate(template.id()));
    }
    
    public void setPredicateWithFormat(String format) {
        setPredicate(NSPredicate.predicateWithFormat(NSString.stringWith(format)));
    }
    
    public void setPredicateWithFormat(String format, String[] arguments) {
        NSMutableArray args = NSMutableArray.arrayWithCapacity(arguments.length);
        for (int i = 0; i < arguments.length; i++)
            args.addObject(NSString.stringWith(arguments[i]));
        
        setPredicate(NSPredicate.predicateWithFormat(NSString.stringWith(format), args));
    }
    
    void setPredicate(NSPredicate predicate) {
        this.currentPredicate = predicate;
        nsPredicateEditor.setObjectValue(predicate);
    }
    
    public void acceptPredicateVisitor(PredicateVisitor visitor, Object context) {
        if (firstTimePredicateUpdate) {
            this.currentPredicate = nsPredicateEditor.predicate();
            firstTimePredicateUpdate = false;
        }
        visitor.visit(new Predicate(this.currentPredicate.id), context);
    }
    
    public void acceptPredicateVisitor(PredicateVisitor visitor) {
        acceptPredicateVisitor(visitor, null);
    }
    
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        
        NSSize size = new NSSize();
        size.width = width;
        size.height = height;
        nsPredicateEditor.setFrameSize(size);
    }
    
    @Override
    public void setSize (Point size) {
        setSize(Math.max (0, size.x), Math.max (0, size.y));
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }
    
    @Override
    protected void checkSubclass () {
    }
    
    @Override
    public void dispose() {
        if (this.isDisposed()) return;
        
        disableNotifications();
        
        unbind("value");
        
        if (kvNotification != null) {
            kvNotification.release();
            kvNotification = null;
        }
        
        for (NSPredicateEditorRowTemplate rowTemplate : rowTemplates) {
            if (rowTemplate != null)
                rowTemplate.release();
        }
        
        rowTemplates.clear();
        
        super.dispose();
    }
    
    @Override
    void createHandle () {
        super.createHandle ();
        
        nsPredicateEditor = (NSPredicateEditor) new NSPredicateEditor().alloc();
        nsPredicateEditor = (NSPredicateEditor) nsPredicateEditor.init();
        
        nsPredicateEditor.setAutoresizingMask(AutoresizingMaskOptions.NSViewWidthSizable.value() |
                                              AutoresizingMaskOptions.NSViewHeightSizable.value());
        
        nsPredicateEditor.setCanRemoveAllRows(false);
        
        view = nsPredicateEditor;
    }
    
    @Override
    public Point computeSize (int wHint, int hHint, boolean changed) {
        checkWidget();
        
        int width = (int) nsPredicateEditor.frame().width;
        int height = (int) nsPredicateEditor.frame().height;
        
        if (wHint != SWT.DEFAULT) 
            width = wHint;
        
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }

        return new Point(width, height);
    }

    // Get the current predicate, 
    // but modified so that 'NOT (predicate)' becomes 'NOT (predicate OR FALSEPREDICATE)' , 
    // which allows NSPredicateEditor to restore simple negated predicates.
    public Predicate getPredicate() {
        return new Predicate(exportPredicate(nsPredicateEditor.predicate()).id);
    }
    
    private id exportPredicate(NSPredicate predicate) {
        if (predicate.isKindOfClass(OS.class_NSComparisonPredicate))
            return predicate;
         
        NSCompoundPredicate nsCompoundPredicate = new NSCompoundPredicate(predicate.id);
        NSArray subpredicates = nsCompoundPredicate.subpredicates();
        
        NSMutableArray updatedSubpredicates = NSMutableArray.arrayWithCapacity(subpredicates.count());
        for (int i = 0; i < subpredicates.count(); i++)
            updatedSubpredicates.addObject(exportPredicate(new NSPredicate(subpredicates.objectAtIndex(i))));
        
        NSCompoundPredicate newCompoundPredicate = (NSCompoundPredicate) new NSCompoundPredicate().alloc();
        if (nsCompoundPredicate.compoundPredicateType() != CompoundPredicateType.NSNotPredicateType.value() || updatedSubpredicates.count() > 1) {
            newCompoundPredicate.initWithType(new NSCompoundPredicate(predicate.id).compoundPredicateType(), updatedSubpredicates);
            return newCompoundPredicate;
        }

        NSMutableArray newOrSubpredicates = NSMutableArray.arrayWithCapacity(2);
        newOrSubpredicates.addObject(updatedSubpredicates.objectAtIndex(0));
        newOrSubpredicates.addObject(NSPredicate.predicateWithValue(false));        

        NSCompoundPredicate newOrPredicate = (NSCompoundPredicate) new NSCompoundPredicate().alloc();
        newOrPredicate.initWithType(CompoundPredicateType.NSOrPredicateType.value(), newOrSubpredicates);

        NSCompoundPredicate newNotPredicate = (NSCompoundPredicate) new NSCompoundPredicate().alloc();
        newNotPredicate.initWithType(CompoundPredicateType.NSNotPredicateType.value(), NSArray.arrayWithObject(newOrPredicate));
        
        return newNotPredicate;
    }
    
    public void updatePredicate(String leftExpr, String rightExpr) {        
        NSPredicate newPredicate = new NSPredicate(updatePredicate(nsPredicateEditor.predicate(), leftExpr, rightExpr));
        
        nsPredicateEditor.setObjectValue(newPredicate); 
    }

    public void updatePredicateWithPredicate(Predicate predicate, String leftExpr, String rightExpr) {
        NSPredicate newPredicate = new NSPredicate(updatePredicate(new NSPredicate(predicate.id()), leftExpr, rightExpr));
        nsPredicateEditor.setObjectValue(newPredicate); 
    }
    
    public HashMap<String, String> getAllComparisonSubpredicatesLeftRightExpressions() {
        HashMap<String, String>lrExprMap = new HashMap<String, String>();
        NSCompoundPredicate predicate = new NSCompoundPredicate(nsPredicateEditor.predicate().id);
        extractLRExpressionsToMap(predicate, lrExprMap);
        
        return lrExprMap;
    }
    
    public HashSet<DynamicRightValuesRowTemplate> allDynamicRowTemplateInstances() {
        return dynamicRowTemplateInstances;
    }
    
    private void bind(String binding, SWTKeyValueCodingDecorator toObject, String keyPath) {
        nsPredicateEditor.bind(NSString.stringWith(binding), toObject, NSString.stringWith(keyPath), null);
    }
    
    private void unbind(String binding) {
        nsPredicateEditor.unbind(NSString.stringWith(binding));
    }
    
    private id updatePredicate(NSPredicate predicate, String leftExpr, String rightExpr) {
        if (predicate.isKindOfClass(OS.class_NSComparisonPredicate)) {
            NSComparisonPredicate cp = new NSComparisonPredicate(predicate.id);
            
            if (leftExpr.equalsIgnoreCase(cp.leftExpression().description().getString())) {
                NSExpression newRightExpr = NSExpression.expressionForConstantValue(NSString.stringWith(rightExpr));
                
                NSComparisonPredicate newPredicate = (NSComparisonPredicate) new NSComparisonPredicate().alloc();
                return newPredicate.initWithLeftExpression(
                        cp.leftExpression(), 
                        newRightExpr, 
                        cp.comparisonPredicateModifier(), 
                        cp.predicateOperatorType(), 
                        cp.options());
            }
            else 
                return predicate;
        }
        else {
            NSArray subpredicates = new NSCompoundPredicate(predicate.id).subpredicates();
            
            NSMutableArray updatedSubpredicates = NSMutableArray.arrayWithCapacity(subpredicates.count());
            for (int i = 0; i < subpredicates.count(); i++)
                updatedSubpredicates.addObject(updatePredicate(new NSPredicate(subpredicates.objectAtIndex(i)), leftExpr, rightExpr));
            
            NSCompoundPredicate newPredicate = (NSCompoundPredicate) new NSCompoundPredicate().alloc();
            return newPredicate.initWithType(new NSCompoundPredicate(predicate.id).compoundPredicateType(), updatedSubpredicates);
        }
    }

    private void extractLRExpressionsToMap(NSCompoundPredicate compoundPredicate, HashMap<String, String> lrExprMap) {
        NSArray subpredicates = compoundPredicate.subpredicates();
        for (int i = 0; i < subpredicates.count(); i++) {
            NSPredicate predicate = new NSPredicate(subpredicates.objectAtIndex(i));
            if (predicate.isKindOfClass(OS.class_NSComparisonPredicate)) {
                NSComparisonPredicate p = new NSComparisonPredicate(predicate.id);
                lrExprMap.put(p.leftExpression().description().getString(), p.rightExpression().description().getString());
            } else {
                extractLRExpressionsToMap(new NSCompoundPredicate(predicate.id), lrExprMap);
            }
        }
    }

    public void addDynamicRowTemplateInstance(DynamicRightValuesRowTemplate newTemplate) {
        boolean foundTemplate = false;
        
        for (DynamicRightValuesRowTemplate t : dynamicRowTemplateInstances) {
            if (t.equals(newTemplate)) {
                t.setPredicate(newTemplate.getPredicate());
                foundTemplate = true;
                break ;
            }
        }
               
        if (!foundTemplate) {
            dynamicRowTemplateInstances.add(newTemplate);
            dynamicRightValuesRowsKeyPaths.add(newTemplate.getKeyPath());
        }
    }

    public void removeDynamicRowTemplateInstance(DynamicRightValuesRowTemplate template) {
        for (DynamicRightValuesRowTemplate t : dynamicRowTemplateInstances) {
            if (t.equals(template)) {
                dynamicRowTemplateInstances.remove(t);
                return;
            }
        }
    }

    public void addMoneyRowTemplateInstance(MoneyRowTemplate template) {
        moneyRowTemplateInstances.add(template);    
    }
    
    public void removeMoneyRowTemplateInstance(MoneyRowTemplate template) {
        moneyRowTemplateInstances.remove(template);
    }
    
    public void addLengthRowTemplateInstance(LengthRowTemplate template) {
        lengthRowTemplateInstances.add(template);    
    }
    
    public void removeLengthRowTemplateInstance(LengthRowTemplate template) {
    	lengthRowTemplateInstances.remove(template);
    }

    public void addWeightRowTemplateInstance(WeightRowTemplate template) {
        weightRowTemplateInstances.add(template);    
    }
    
    public void removeWeightRowTemplateInstance(WeightRowTemplate template) {
    	weightRowTemplateInstances.remove(template);
    }
    
    public void refreshLayout() {
        for (DynamicRightValuesRowTemplate template : dynamicRowTemplateInstances)
            template.refreshLayout();
        
        for (MoneyRowTemplate template : moneyRowTemplateInstances)
            template.refreshLayout();
        
        for (LengthRowTemplate template : lengthRowTemplateInstances)
            template.refreshLayout();
        
        for (WeightRowTemplate template : weightRowTemplateInstances)
            template.refreshLayout();
    }
    
    public void refreshUI() {
        for (DynamicRightValuesRowTemplate template : dynamicRowTemplateInstances)
            template.refreshUI();
    }
    
    private void handlePredicateChanged() {
        if (lastNumberOfRows > 0 && !checkIfRowWasRemoved())
            refreshUI();
    
        lastNumberOfRows = nsPredicateEditor.numberOfRows();
    }
    
    private boolean checkIfRowWasRemoved() {
        long currentNumberOfRows = nsPredicateEditor.numberOfRows();
        
        if (currentNumberOfRows == lastNumberOfRows)
            return countDynamicRowsPredicates(currentPredicate.predicateFormat().getString()) < dynamicRowTemplateInstances.size();
        else
            return currentNumberOfRows < lastNumberOfRows;
    }
    
    private int countDynamicRowsPredicates(String predicateFormat) {
        int count = 0;
        
        for (String keyPath : dynamicRightValuesRowsKeyPaths)
            count += countPredicates(predicateFormat, keyPath + " ");
        
        return count;
    }
    
    private int countPredicates(String predicateFormat, String keyPath) {
        if (predicateFormat == null) return 0;
        
        Pattern p = Pattern.compile(keyPath);
        Matcher m = p.matcher(predicateFormat);
        int count = 0;
        while (m.find())
            count++;
        
        return count;
    }
}
