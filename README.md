# VerLog

**VerLog: Enhancing Release Note Generation for Android Apps using Large Language Models**

| | |
|---|---|
| Original artifact | <https://zenodo.org/records/15200248> |
| Imported from | the publications page |
| Tool | `pubs2github` |


---

## Contents

The artifact contains 288 file(s) including Python, Java, Shell scripts, Config files, Data files, and Documentation.

```
├── __MACOSX
│   ├── app
│   │   ├── demo-app
│   │   ├── example-out
│   │   ├── scripts
│   │   ├── verlog
│   │   ├── ._demo-app
│   │   ├── ._Dockerfile
│   │   ├── ._example-out
│   │   ├── ._requirements.txt
│   │   ├── ._runVerlogDemo.sh
│   │   ├── ._scripts
│   │   ├── ._verlog
│   │   ├── ._Verlog-code-1.0-SNAPSHOT.jar
│   │   └── ._verlog.sh
│   └── ._app
├── app
│   ├── demo-app
│   ├── example-out
│   │   └── com.fmsys.snapdrop
│   ├── scripts
│   │   ├── get_package_name.sh
│   │   └── get_version_name.sh
│   ├── verlog
│   │   ├── verlog_differ
│   │   └── verlog_summarizer
│   ├── Dockerfile
│   ├── requirements.txt
│   ├── runVerlogDemo.sh
│   ├── Verlog-code-1.0-SNAPSHOT.jar
│   └── verlog.sh
├── benchmark_apps_versions.csv
└── README.md
```

---

## Original `README.md` (from the upstream artifact)

# VerLog: Automated Release Note Generation for Android Apps

VerLog generates/enhances release note generation by leveraging Large Language Models (LLMs) with graph-based code analysis, creating comprehensive and readable release notes from code changes.

## Quick Start

### Use Docker to Run Verlog

The VerLog artifact is also available as a [Docker image](https://hub.docker.com/r/jarweigh/verlog-artifact) for convenient artifact evaluation

Please use the architecture-specific image that matches your system:

- For x86/AMD64 systems (most Linux/Windows PCs):

  ```bash
  docker pull jarweigh/verlog-artifact:latest-amd64
  ```

- For ARM64 systems (Apple M1/M2 Macs):

  ```bash
  docker pull jarweigh/verlog-artifact:latest-arm64
  ```

To run it with your [LLM API key](https://platform.deepseek.com/api_keys)

```bash
# Run the container with your DeepSeek API key
docker run -it -e DS_API_KEY="your_deepseek_api_key" --name verlog-container jarweigh/verlog-artifact:latest-[amd64|arm64]
```

### Assessing Availability

1. Check that all necessary components are included:
   - Verify the presence of the compiled JAR file (`Verlog-code-1.0-SNAPSHOT.jar`)
   - Confirm demo app files in `demo-app/com.fmsys.snapdrop`
   - Check that all required scripts and Python/JAVA source code are included in `verlog`

2. Examine the reference application:
   - Verify the APK files in `demo-app/com.fmsys.snapdrop/built_apks`
   - Check the repository snapshots in `demo-app/com.fmsys.snapdrop/tagged_repos`
   - Confirm example outputs in `example-out`

3. Review the `benchmark_apps_versions.csv` file to understand the complete evaluation dataset used in the paper.

### Assessing Functionality

1. [Skip this step if using Docker to run Verlog] Set up the environment ()

   - Install required dependencies using `pip install -r requirements.txt`

   - Ensure Java ≥ 1.8 is available

   - Configure Android platform JARs (use your own or follow the instructions to install them)

   - Obtain an API key from DeepSeek and set it as an environment variable:

     ```bash
     export DS_API_KEY="YOUR_API_KEY"
     ```

2. Run the demo:

   - Execute `bash runVerlogDemo.sh`
   - Verify that the tool processes the PairDrop app across multiple versions
   - Check the generated release notes in `out/com.fmsys.snapdrop/*/release_note.DeepSeek.txt`
   - Compare these with the paper's reported effectiveness metrics

3. Review the intermediate outputs:

   - Examine `out/com.fmsys.snapdrop/*/diff_results` to see the code change detection
   - Look at `out/com.fmsys.snapdrop/*/prompts` to understand how changes are structured for the LLM
   - Review `out/com.fmsys.snapdrop/*/rn_entries` to see individual release note entries

### Assessing Reusability

1. Understand the tool's customization options:
   - Examine the system prompts in `verlog/verlog_summarizer/assets`
   - Check `verlog/verlog_summarizer/summarizer/llm_assistant.py` to see how different LLMs can be integrated

2. Test adaptability to other apps:
   - Select an entry from `benchmark_apps_versions.csv`
   - Download the corresponding repository and build APKs (More details can be found in Section [Usage](#usage)) in this doc.
   - Run VerLog using the documentation in this README
   - Verify that meaningful release notes are generated

3. Explore programmability:
   - Review the source code organization to understand key components in `verlog/`
   - Check how the differencing engine (`verlog_differ`) interfaces with the summarizer (`verlog_summarizer`)
   - Examine the JSON format for code changes in the outputs `example-out/com.fmsys.snapdrop/v1.10.1-v1.11.0/diff_results`
   - Verify that the tool can be integrated into existing workflows



## Requirements

- Java ≥ 1.8
- Python ≥ 3.7
- Android SDK

## Installation

### 1. Setup Android JARs

If you don't have Android JARs in your `$ANDROID_HOME/platforms`:

```bash
git clone https://github.com/Sable/android-platforms.git
```

### 2. Install Python Dependencies

```bash
pip install -r requirements.txt
```

## Usage

### Preparing Your Application

1. Clone the app repository:

   bash

   ```bash
   git clone https://github.com/example/app.git
   ```

2. Build the app without obfuscation:

   bash

   ```bash
   ./gradlew assembleDebug
   ```

3. Access both reference (old) and target (new) versions: Using git tags:

   bash

   ```bash
   git checkout <tag-name>
   ```

   Or downloading directly:

   bash

   ```bash
   wget https://github.com/example/app/releases/download/<tag-name>/app-<tag-name>.zip
   ```

### Generating Release Notes

Run VerLog with the following command:

```bash
bash ./verlog.sh [OPTIONS]
```

#### Options

| Option                            | Description                                     |
| --------------------------------- | ----------------------------------------------- |
| `--android-sdk-path <path>`       | Path to the Android SDK                         |
| `--git-repo <path>`               | Path to the git repository                      |
| `--ref-apk <path>`                | Path to the reference/base APK file             |
| `--ref-version <version>`         | Reference/base version tag                      |
| `--ref-repo-dir <path>`           | Path to the reference/base repository directory |
| `--tgt-apk <path>`                | Path to the target/release APK file             |
| `--tgt-version <version>`         | Target/release version tag                      |
| `--tgt-repo-dir <path>`           | Path to the target/release repository directory |
| `--app-description <description>` | Description of the app                          |
| `--model <model>`                 | LLM Model for summarization                     |
| `--exact-model-name <name>`       | Exact model name for summarization              |
| `--system-prompt-file <file>`     | System prompt file for summarization            |
| `--output-dir <dir>`              | Output directory for results                    |

## Customization

### LLM Model Selection

The paper uses `gpt-4o-mini`, but you can use other models by extending the `LLM` class in `llm_assistant.py`:

python

```python
class LLM(ABC):
    @abstractmethod
    def summarize(self, prompt, system_message, exact_model_name):
        pass
```

### Supporting Other Programming Languages

VerLog's design is language-agnostic. Ensure your differencing output follows this JSON schema:

```json
{
  "added_classes": [],
  "modified_classes": [
    {
      "class_name": "path/to/Class.java",
      "ADDED_METHOD_IN_MODIFIED_CLASS": [],
      "MODIFIED_METHOD_IN_REF_CLASS": [
        {
          "method_name": "<class.path.ClassName: returnType methodName(paramTypes)>",
          "line_number": "41-160",
          "reachable_methods": []
        }
      ],
      "MODIFIED_METHOD_IN_TGT_CLASS": [
        {
          "method_name": "<class.path.ClassName: returnType methodName(paramTypes)>",
          "line_number": "41-160",
          "reachable_methods": []
        }
      ],
      "DELETED_METHOD_IN_MODIFIED_CLASS": []
    }
  ],
  "deleted_classes": []
}
```

Note: Method names use Soot's signature format. Relevant parsing functions are available in `string_util.py`.

### Customizing Exemplars

You can customize exemplars based on various classification criteria. By default, we include three example exemplars in the system prompt, but you can decouple them for adaptive exemplar selection.

## Demo: Running VerLog on PairDrop

We'll demonstrate VerLog using [PairDrop](https://github.com/fm-sys/snapdrop-android), an open-source Android app with 900+ GitHub stars.

### Setup

1. Export your LLM API key (this demo uses DeepSeek for cost efficiency):

   ```bash
   export DS_API_KEY="YOUR_API_KEY"
   ```

   You can obtain an API key from 

   https://platform.deepseek.com/api_keys

2. Ensure FlowDroid has access to Android platform JARs (use your `$ANDROID_HOME/platforms` or install them in `./android-platforms`)

3. Run the demo:

   ```bash
   bash runVerlogDemo.sh
   ```

4. View generated release notes:

   ```bash
   for file in out/*/*/release_note.DeepSeek.txt; do 
     echo -e "$file:"; 
     cat $file; 
     echo -e "\n\n"; 
   done
   ```

Output is stored in `out/`, including all intermediate files and final release notes.

