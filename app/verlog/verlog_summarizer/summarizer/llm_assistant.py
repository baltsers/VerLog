import os
from openai import OpenAI
from abc import ABC, abstractmethod



class LLM(ABC):
    @abstractmethod
    def summarize(self, prompt, system_message, exact_model_name):
        pass


class ChatGPT(LLM):
    def summarize(self, prompt, system_message, exact_model_name):
        assert os.environ.get('OPENAI_API_KEY') is not None, "Please set the OPENAI_API_KEY environment variable."
        client = OpenAI()
        messages = [
            {"role": "system", "content": system_message},
            {"role": "user", "content": prompt}
        ]
        completion = client.chat.completions.create(
            model=exact_model_name,
            messages=messages
        )
        response_message = completion.choices[0].message.content
        return response_message

class DeepSeek(LLM):
    def summarize(self, prompt, system_message, exact_model_name='chat'):
        assert os.environ.get('DS_API_KEY') is not None, "Please set the DS_API_KEY environment variable."
        DS_API_KEY = os.environ.get('DS_API_KEY')
        client = OpenAI(api_key=DS_API_KEY, base_url="https://api.deepseek.com")
        messages = [
            {"role": "system", "content": system_message},
            {"role": "user", "content": prompt}
        ]
        completion = client.chat.completions.create(
            model=f'deepseek-chat',  # deepseek-chat
            messages=messages,
            stream=False
        )
        response_message = completion.choices[0].message.content
        return response_message


def model_factory(model_name):
    # Expand your models here    
    # if model_name == 'LLama3':
    #     return llm_assistant.LLama3()
    # elif model_name == 'Claude':
    #     return llm_assistant.Claude()
    # elif model_name == 'CodeLlama':
    #     return llm_assistant.CodeLlama()
    # elif model_name == 'DeepSeek':
    #     return llm_assistant.DeepSeek()
    # else:
    if model_name == 'DeepSeek':
        return DeepSeek()
    return ChatGPT()


class Summarizer:
    def __init__(self, model, exact_model_name, system_message):
        self._model = model
        self._exact_model_name = exact_model_name
        self._system_message = system_message
        self._results = []
        self._synthetic_result = ""

    def set_model(self, model):
        self._model = model

    def __extract_summarization(self, response_message):
        # In the response message, the real summarization is within the '{}' brackets
        if '{' not in response_message:
            return response_message
        return response_message.split('{')[1].split('}')[0]

    def summarize(self, prompt):
        response_message = self._model.summarize(prompt, self._system_message, self._exact_model_name)
        summarization = self.__extract_summarization(response_message)
        self._results.append(summarization)
        return summarization

    def get_summarization_results(self):
        return self._results

    def final_summarization(self, final_prompt):
        self._synthetic_result = self._model.summarize(final_prompt, self._system_message, self._exact_model_name)
        return self._synthetic_result

    def serialize_results(self, path):
        with open(path, 'w') as f:
            for i, result in enumerate(self._results, start=1):
                f.write(f"Summarization {i}. {result}\n")
        with open(path + ".final", 'w') as f:
            f.write(f"Synthetic Summarization:\n {self._synthetic_result}\n")

