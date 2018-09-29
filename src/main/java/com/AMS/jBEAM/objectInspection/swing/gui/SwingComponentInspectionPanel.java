package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.InspectionLink;
import com.AMS.jBEAM.objectInspection.InspectionUtils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SwingComponentInspectionPanel extends JPanel
{
    public SwingComponentInspectionPanel(Component component, List<Object> subComponentHierarchy) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Component Hierarchy"));

        List<InspectionLink> componentInspectionHierarchy = createComponentInspectionHierarchy(component, subComponentHierarchy);
        JTree tree = SwingInspectionUtils.createInspectionLinkTree(componentInspectionHierarchy);
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        JScrollPane scrollPane  = new JScrollPane(tree);
        add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    private static List<InspectionLink> createComponentInspectionHierarchy(Component component, List<Object> subComponentHierarchy) {
        List<Object> componentHierarchy = new ArrayList<>();
        for (Component curComponent = component; curComponent != null; curComponent = curComponent.getParent()) {
            componentHierarchy.add(0, curComponent);
        }
        componentHierarchy.addAll(subComponentHierarchy);
        return createInspectionLinkHierarchy(componentHierarchy);
    }

    private static List<InspectionLink> createInspectionLinkHierarchy(List<Object> hierarchy) {
        Map<Object, List<Field>> fieldsByObject = new HashMap<>();
        // Traverse hierarchy from child to parent because usually children are members of parents and not vice versa
        for (int i = hierarchy.size() - 1; i >= 0; i--) {
            Object object = hierarchy.get(i);
            fieldsByObject.put(object, new ArrayList<>());
            addAll(fieldsByObject, InspectionUtils.findFieldValues(object, fieldsByObject.keySet()));
        }
        return hierarchy.stream()
                .map(object -> createInspectionLink(object, fieldsByObject.get(object)))
                .collect(Collectors.toList());
    }

    private static <S, T> void addAll(Map<S, List<T>> aggregated, Map<S, List<T>> toAdd) {
        for (S key : toAdd.keySet()) {
            List<T> valueList = aggregated.get(key);
            valueList.addAll(toAdd.get(key));
        }
    }

    private static InspectionLink createInspectionLink(Object object, List<Field> fields) {
        String linkText = object.getClass().getSimpleName();
        if (!fields.isEmpty()) {
            String fieldText = fields.stream()
                    .map(InspectionUtils::formatField)
                    .collect(Collectors.joining(", "));
            linkText += " (" + fieldText + ")";
        }
        return new InspectionLink(object, linkText);
    }
}
