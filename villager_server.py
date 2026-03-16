from flask import Flask, request, jsonify
import requests
import traceback

app = Flask(__name__)

OLLAMA_URL = "http://127.0.0.1:11434/api/generate"
MODEL = "phi3"


def ask_llm(prompt: str):
    try:
        response = requests.post(
            OLLAMA_URL,
            json={
                "model": MODEL,
                "prompt": prompt,
                "stream": False
            },
            timeout=120
        )

        response.raise_for_status()
        data = response.json()

        return data.get("response", "")

    except Exception:
        print("ERROR contacting Ollama:")
        print(traceback.format_exc())
        return None


@app.route("/villager", methods=["POST"])
def villager():
    try:
        data = request.json
        prompt = data.get("prompt", "")

        print("Received prompt:")
        print(prompt)

        llm_reply = ask_llm(prompt)

        if llm_reply is None:
            return jsonify({
                "reply": "Hmm...",
                "actions": []
            })

        print("LLM RAW RESPONSE:")
        print(llm_reply)

        # --- Expect JSON from the model ---
        # If model fails to return JSON, fallback

        try:
            import json
            parsed = json.loads(llm_reply)
            return jsonify(parsed)

        except Exception:
            print("LLM did not return valid JSON")
            print(traceback.format_exc())

            return jsonify({
                "reply": llm_reply.strip(),
                "actions": []
            })

    except Exception:
        print("SERVER ERROR:")
        print(traceback.format_exc())

        return jsonify({
            "reply": "The villager looks confused.",
            "actions": []
        })


if __name__ == "__main__":
    print("")
    print("=====================================")
    print(" SmartVillagers AI server running")
    print(" http://127.0.0.1:8000")
    print("=====================================")
    print("Waiting for villager prompts...\n")

    app.run(host="127.0.0.1", port=8000)
