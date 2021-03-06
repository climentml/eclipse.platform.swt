package org.eclipse.swt.util;

import java.util.Map;
import org.eclipse.swt.internal.cocoa.NSArray;
import org.eclipse.swt.internal.cocoa.NSAttributedString;
import org.eclipse.swt.internal.cocoa.NSExpression;
import org.eclipse.swt.internal.cocoa.NSFont;
import org.eclipse.swt.internal.cocoa.NSMenuItem;
import org.eclipse.swt.internal.cocoa.NSPopUpButton;
import org.eclipse.swt.internal.cocoa.NSRect;
import org.eclipse.swt.internal.cocoa.NSSize;
import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.NSTextField;
import org.eclipse.swt.internal.cocoa.OS;

public class GuiUtil {
    private static boolean useSmallFonts = System.getProperty("org.eclipse.swt.internal.carbon.smallFonts") != null;
    
    public static boolean useSmallFonts() {
        return useSmallFonts;
    }
    
    public static double /*float*/ computeMinimumHeightOfOneLineTextInCurrentGraphicsContext() {
        NSSize wrapSize = new NSSize();
        wrapSize.width = 1000;
        wrapSize.height = 1000;
        NSAttributedString attribStr = (NSAttributedString) new NSAttributedString().alloc();
        attribStr.initWithString(NSString.stringWith("ABC"));
        NSRect rect = attribStr.boundingRectWithSize(wrapSize, OS.NSStringDrawingUsesFontLeading);
        attribStr.release();
        
        return rect.height;
    }
    
    public static void adjustTextFieldForSmallControlSize(NSTextField textField) {
        textField.cell().setControlSize(OS.NSSmallControlSize);
        textField.setFont(NSFont.systemFontOfSize(NSFont.systemFontSizeForControlSize(OS.NSSmallControlSize)));
    }
    
    public static void changeTextFieldSize(NSTextField textField, double /*float*/ width, double /*float*/ height) {
        NSSize size = new NSSize();
        size.width = width;
        size.height = height;;
        
        textField.setFrameSize(size);
    }

    public static void initTextField(NSTextField textField, int width, int height, int smallControlExtraHeight) {
        if (GuiUtil.useSmallFonts()) {
            textField.initWithFrame(new NSRect());
            adjustTextFieldForSmallControlSize(textField);
            changeTextFieldSize(textField, width, GuiUtil.computeMinimumHeightOfOneLineTextInCurrentGraphicsContext() + smallControlExtraHeight);
        } else {
            NSRect rect = new NSRect();
            rect.width = width;
            rect.height = height;
            textField.initWithFrame(rect);
        }
    }
    
    public static void replaceKeyPathWithTitle(NSPopUpButton button, Map<String, String> keyPathToTitleMap) {
        NSArray items = button.itemArray();
        for (int i = 0; i < items.count(); i++) {
            NSMenuItem item = new NSMenuItem(items.objectAtIndex(i).id);
            NSExpression keyPathExpression = new NSExpression(item.representedObject());
            String title = keyPathToTitleMap.get(keyPathExpression.keyPath().getString());
            if (title != null)
                item.setTitle(NSString.stringWith(title));
        }
    }
}
