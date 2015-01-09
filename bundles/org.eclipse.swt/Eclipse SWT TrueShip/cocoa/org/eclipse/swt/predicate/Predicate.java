package org.eclipse.swt.predicate;

import java.util.*;

import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.keyvaluecoding.*;

public class Predicate {
    NSPredicate nsPredicate;
    
    public static Predicate predicateWithFormat(String format, List<String> args) {
        NSMutableArray predicateArgs = NSMutableArray.arrayWithCapacity(args.size());
        for (String arg : args)
            predicateArgs.addObject(NSString.stringWith(arg));
        
        NSPredicate nsPredicate = NSPredicate.predicateWithFormat(NSString.stringWith(format), predicateArgs);
        
        return new Predicate(nsPredicate);
    }
    
    public long /*int*/ id() {
        return nsPredicate.id;
    }
    
    private Predicate(NSPredicate predicate) {
        this.nsPredicate = predicate;
    }

    public Predicate(long /*int*/ id) {
        this.nsPredicate = new NSPredicate(id);
    }

    public String predicateFormat() {
        return nsPredicate.predicateFormat().getString();
    }

    public boolean evaluateWithObject(KeyValueCodingDecorator kvObject) {
        return nsPredicate.evaluateWithObject(new id(kvObject.id()));
    }
    
    public boolean isComparisonPredicate() {
        return nsPredicate.isKindOfClass(OS.class_NSComparisonPredicate);
    }
    
    public boolean isCompoundPredicate() {
        return nsPredicate.isKindOfClass(OS.class_NSCompoundPredicate);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof Predicate))
            return false;
        
        Predicate predicate = (Predicate)o;
        
        if (this.nsPredicate.id != predicate.nsPredicate.id)
            return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return nsPredicate.hashCode();
    }
}
