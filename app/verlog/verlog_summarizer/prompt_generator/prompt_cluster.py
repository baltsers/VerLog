from collections import defaultdict
from typing import List, Tuple, Dict, Any


def prune_hierarchy_tree(method_tuples: List[Tuple[str, int]]) -> Dict[str, Tuple[str, int]]:
    """
    Prune a hierarchy tree based on the specified rules to minimize the number of nodes.
    
    Rules:
    1. If a class has >= 2 methods with v=1, fold them to the class level
    2. After class-level folding, if a package has >= 2 classes with v=1, fold them to package level
    3. Continue until reaching the root OR until any node has at most 1 child with v=1
    
    Args:
        method_tuples: List of tuples (method_name, v)
        
    Returns:
        Dictionary mapping method_name to (pruned_name, pruned_value)
    """
    # Step 1: Group methods by class
    classes = defaultdict(list)
    for method_name, value in method_tuples:
        parts = method_name.split('.')
        class_name = '.'.join(parts[:-1])  # Everything except the method name
        classes[class_name].append((method_name, value))
    
    # Step 2: Perform class-level folding
    pruning_map = {}  # Maps method_name to pruned_name
    pruned_values = {}  # Maps pruned_name to its computed value
    
    for class_name, methods in classes.items():
        # Count methods with v=1
        v1_methods = [(name, val) for name, val in methods if val == 1]
        
        # Apply class-level folding if there are >= 2 methods with v=1
        if len(v1_methods) >= 2:
            # Fold v=1 methods to class level
            for method_name, _ in v1_methods:
                pruning_map[method_name] = class_name
            # Set value for the folded class
            pruned_values[class_name] = len(v1_methods)
        elif len(v1_methods) == 1 and len(methods) == 1:
            # Single method with v=1 in class - keep as is for now
            method_name = v1_methods[0][0]
            pruning_map[method_name] = method_name
            pruned_values[method_name] = 1
    
    # Step 3: Group classes by package at each level
    # We only process packages from most specific to most general
    # and stop when the stopping condition is met
    packages = defaultdict(list)
    for class_name in classes.keys():
        parts = class_name.split('.')
        # Generate all possible package levels
        for i in range(1, len(parts)):
            package_name = '.'.join(parts[:i])
            packages[package_name].append(class_name)
    
    # Process packages from most specific to least specific
    processed_nodes = set()  # Nodes already processed (folded or checked)
    
    for package_name, package_classes in sorted(packages.items(), key=lambda x: -len(x[0].split('.'))):
        # Skip if this package has already been processed
        if package_name in processed_nodes:
            continue
            
        # Find classes that have v=1 after class-level folding
        v1_classes = []
        
        for class_name in package_classes:
            # Skip if this class has already been processed
            if class_name in processed_nodes:
                continue
                
            class_methods = classes[class_name]
            
            # Class with multiple methods that has been folded to v=1
            if class_name in pruned_values and pruned_values[class_name] == 1:
                v1_classes.append(class_name)
                continue
                
            # Class with a single v=1 method that wasn't folded
            v1_methods = [(m, v) for m, v in class_methods if v == 1]
            if len(v1_methods) == 1 and len(class_methods) == 1:
                method_name = v1_methods[0][0]
                if method_name not in pruning_map or pruning_map[method_name] == method_name:
                    v1_classes.append(class_name)
        
        # Mark this package as processed
        processed_nodes.add(package_name)
        
        # STOPPING CONDITION: If this package has at most 1 child with v=1, don't fold
        if len(v1_classes) <= 1:
            # Mark all these classes as processed too
            for class_name in v1_classes:
                processed_nodes.add(class_name)
            continue
        
        # If we get here, apply package-level folding (>= 2 classes with v=1)
        pruned_values[package_name] = len(v1_classes)
        
        # Update pruning map for these classes and their methods
        for class_name in v1_classes:
            processed_nodes.add(class_name)
            class_methods = classes[class_name]
            
            # If class was folded, update all its v=1 methods
            if class_name in pruned_values and pruned_values[class_name] > 1:
                for method_name, value in class_methods:
                    if value == 1:
                        pruning_map[method_name] = package_name
            # If class wasn't folded, it has a single v=1 method
            else:
                v1_methods = [(m, v) for m, v in class_methods if v == 1]
                if v1_methods:
                    method_name = v1_methods[0][0]
                    pruning_map[method_name] = package_name
    
    # Step 4: Create the final mapping with computed values
    result = {}
    
    for method_name, value in method_tuples:
        # Methods with v > 1 are never folded
        if value > 1:
            result[method_name] = (method_name, value)
        else:
            # Check if this method was pruned
            pruned_name = pruning_map.get(method_name, method_name)
            
            if pruned_name != method_name:
                # Method was folded, use the computed value
                pruned_value = pruned_values.get(pruned_name, 1)
                result[method_name] = (pruned_name, pruned_value)
            else:
                # Method wasn't folded, keep original
                result[method_name] = (method_name, value)
    
    return result

