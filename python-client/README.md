# FinTrade Python Client

Standalone Tkinter interface for the FinTrade simulator. This milestone contains
login and registration screens with local validation only; it does not connect
to the Java trading server yet.

## Structure

```text
python-client/
├── main.py
└── fintrade_client/
    ├── app.py
    ├── theme.py
    └── views/
        ├── base_auth_view.py
        ├── components.py
        ├── login_view.py
        └── register_view.py
```

## Requirements

- Python 3.11 or newer recommended
- Tkinter (included with the standard Python installer)

The Homebrew Python 3.14 installation in this workspace needs its matching Tk
binding:

```bash
brew install python-tk@3.14
```

## Run

From the repository root, use the provided launcher:

```bash
bash python-client/run.sh
```

Do not run this client with `/usr/bin/python3`. Apple's system Python uses Tk
8.5, which does not render this interface correctly in modern macOS dark mode.
The launcher deliberately uses Homebrew Python 3.14 with Tk 9.1.

The client uses only the Python standard library, so there is no `pip install`
step.
