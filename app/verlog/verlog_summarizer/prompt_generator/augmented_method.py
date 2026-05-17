
class AugmentedMethod:
    def __init__(self, 
                 method_name, 
                 method_changed_category, 
                 method_line_nums, 
                 reachable_methods, 
                 diff_hunk_lines):
        self.method_name = method_name
        self.method_changed_category = method_changed_category
        self.method_line_nums = method_line_nums
        self.reachable_methods = reachable_methods
        self.diff_hunk_lines = diff_hunk_lines

    def __str__(self):
        return self.generate_prompt()

    def __repr__(self):
        return self.__str__()
    
    def generate_prompt(self):
        prompt = ''
        prompt += f'**Method Signature**: {self.method_name}\n'
        prompt += f'**Method Changed Category**: {self.method_changed_category}\n'
        #prompt += f'**Method Line Numbers**: {self.method_line_nums}\n'
        prompt += f'**Callees of the Method**: \n'
        for i, reachable_method in enumerate(self.reachable_methods):
            prompt += f'\t{i+1}. {reachable_method}\n'
        prompt += f'**Changed Lines of Code**:\n'
        for i, diff_hunk_line in enumerate(self.diff_hunk_lines):
            prompt += f'\t{diff_hunk_line}'
        return prompt
    
    def pretty_print(self):
        print(self.generate_prompt())

    def is_in_reachable_methods(self, method_name):
        return method_name in self.reachable_methods
        