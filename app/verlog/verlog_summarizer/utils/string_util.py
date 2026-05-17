"""String utility functions"""

def extract_clsname_from_screenshot_file_name(screenshot_file_name):
    """Usage: version-classname.png -> classname 

    Args:
        screenshot_file_name (str): screenshot file name

    Returns:
        str: class name
    """
    return screenshot_file_name.split('-')[1].split('.')[0]

def extract_clsname_from_class_signature(cls_signature):
    """Usage: a.b.c.ClassName -> ClassName
    
    Args:
        cls_signature (str): class signature (fully qualified class name)

    Returns:
        str: class name
    """
    return cls_signature.split('.')[-1]

def extract_package_name_from_diff_file(diff_file):
    """Usage: a/b/c/com.foo.bar-diff.json -> com.foo.bar

    Args:
        diff_file (str): diff file path

    Returns:
        str: package name
    """
    return diff_file.split('/')[-1].split('-')[0]

def extract_function_name_from_method_signature(method_signature):
    """Usage: <ca.cmetcalfe.locationshare.MainActivity: void updateLocation(android.location.Location)> -> updateLocation
    
    Args:
        method_signature (str): method signature (fully qualified method name)

    Returns:
        str: method name
    """
    return method_signature.split(' ')[-1].split('(')[0]

def extract_class_method_name_from_method_signature(method_signature):
    class_name = method_signature.split(':')[0].split('<')[-1]
    method_name = extract_function_name_from_method_signature(method_signature)
    return class_name + '.' + method_name

def extract_non_parameterized_method_signature(method_signature):
    """Usage: <ca.cmetcalfe.locationshare.MainActivity: void updateLocation(android.location.Location)> -> <ca.cmetcalfe.locationshare.MainActivity: void updateLocation>
    
    Args:
        method_signature (str): method signature (fully qualified method name)

    Returns:
        str: non-parameterized method signature
    """
    return method_signature.split('(')[0]

def convert_classname_to_path(classname):
    """Usage: com.foo.bar -> com/foo/bar.java

    Args:
        classname (str): class name

    Returns:
        str: class path
    """
    return classname.replace('.', '/') + '.java'

def format_method_line_nums(unformatted_line_nums):
    """Usage: "1-10" -> (1, 10)

    Args:
        unformatted_line_nums (str): unformatted line numbers

    Returns:
        Tuple[int, int]: formatted line numbers
    """
    return tuple(map(int, unformatted_line_nums.split('-')))

def draw_progress_bar(current, total, bar_length=25):
    fraction = current / total
    arrow = int(fraction * bar_length - 1) * '=' + '>'
    padding = (bar_length - len(arrow)) * ' '
    percent = int(fraction * 100)
    return f"Summarizing: [{arrow}{padding}] {percent}% ({current}/{total})"



