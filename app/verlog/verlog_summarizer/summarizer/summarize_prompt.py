import os
from summarizer import llm_assistant
import sys



def system_message_factory(system_prompt_file):
    # Add your exemplars here
    with open(system_prompt_file, 'r') as f:
        return f.read()


def run(args):
    input_prompt_file = args.input_prompt_file
    model_name = args.model
    exact_model_name = args.exact_model_name
    output_dir = args.output_dir
    system_prompt_file = args.system_prompt_file

    model = llm_assistant.model_factory(model_name)
    system_message = system_message_factory(system_prompt_file)

    input_prompt_name = os.path.basename(input_prompt_file)
    output_file = f'{output_dir}/{input_prompt_name}.{model_name}'
    #if os.path.isfile(output_file):
    #    sys.exit(f'File {output_file} already exists. Exiting...')

    summarizer = llm_assistant.Summarizer(model, exact_model_name, system_message)

    with open(input_prompt_file, 'r') as f:
        input_prompt = f.read()

    summarization_result = summarizer.summarize(input_prompt)

    with open(output_file, 'w') as f:
        f.write(summarization_result)
