import os
import argparse
import llm_assistant


def system_message_factory(system_prompt_file):
    with open(system_prompt_file, 'r') as f:
        return f.read()

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--input-dir', type=str, required=True)
    parser.add_argument('--model', type=str, required=False, choices=['ChatGPT', 'DeepSeek'])
    parser.add_argument('--exact-model-name', type=str, default="gpt-4o-mini")
    parser.add_argument('--commit-messages-file', type=str, required=True)
    parser.add_argument('--system-prompt-file', type=str, required=True)
    parser.add_argument('--output-dir', type=str, required=True)

    args = parser.parse_args()

    input_prompt_dir = args.input_dir
    model_name = args.model
    exact_model_name = args.exact_model_name
    system_prompt_file = args.system_prompt_file
    commit_messages_file = args.commit_messages_file
    output_dir = args.output_dir

    model =llm_assistant.model_factory(model_name)
    system_message = system_message_factory(system_prompt_file)

    synthesize_summarizer = llm_assistant.Summarizer(model, exact_model_name, system_message)

    input_summarization_results = []

    with open(commit_messages_file, 'r') as f:
        commit_messages_list = f.readlines()

    commit_messages = []
    commit_messages = set(line.strip() for line in commit_messages_list)

    for i, input_prompt_file in enumerate(os.listdir(input_prompt_dir), start=1):
        with open(f'{input_prompt_dir}/{input_prompt_file}', 'r') as f:
            input_summarization_result = f.read().strip()
            input_summarization_results.append(f'{i}.\t{input_summarization_result}')

    input_summarization_prompt = "Commit Messages:\n" + '\n'.join(commit_messages) + '\n\n'    
    input_summarization_prompt += "Generated Release Notes Entry:\n" + '\n'.join(input_summarization_results)

    synthesize_result = synthesize_summarizer.summarize(input_summarization_prompt)

    output_file = f'{output_dir}/release_note.{model_name}.txt'

    with open(output_file, 'w') as f:
        f.write(synthesize_result)
