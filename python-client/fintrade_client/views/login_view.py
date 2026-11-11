"""Login screen with local-only validation."""

from __future__ import annotations

import re
import queue
import threading
import tkinter as tk
from collections.abc import Callable

from .. import theme
from ..services.authentication_service import AuthenticationService
from .base_auth_view import BaseAuthView
from .components import FormField


EMAIL_PATTERN = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")


class LoginView(BaseAuthView):
    def __init__(
        self,
        parent: tk.Widget,
        *,
        on_login: Callable[[], None],
        on_register: Callable[[], None],
        authentication_service: AuthenticationService,
    ) -> None:
        super().__init__(
            parent,
            on_login=on_login,
            on_register=on_register,
            authentication_service=authentication_service,
        )

        self.email = tk.StringVar()
        self.password = tk.StringVar()
        self.password_visible = tk.BooleanVar(value=False)
        self._login_in_progress = False
        self._login_results: queue.Queue[
            tuple[str, object]
        ] = queue.Queue()

        self.add_heading(
            "Welcome back",
            "Sign in to access your simulated trading account.",
        )

        self.email_field = FormField(self, "Email", self.email)
        self.email_field.pack(fill="x", pady=(0, 14))

        self.password_field = FormField(
            self,
            "Password",
            self.password,
            password=True,
        )
        self.password_field.pack(fill="x")

        tk.Checkbutton(
            self,
            text="Show password",
            variable=self.password_visible,
            command=self._toggle_password,
            background=theme.CARD_BACKGROUND,
            activebackground=theme.CARD_BACKGROUND,
            foreground=theme.MUTED,
            activeforeground=theme.TEXT,
            selectcolor=theme.CARD_BACKGROUND,
            font=(theme.FONT_FAMILY, 10),
        ).pack(anchor="w", pady=(10, 0))

        self.add_primary_button("Sign in", self.submit)
        self.add_status()
        self.add_navigation(
            "New to FinTrade?",
            "Create an account",
            self.on_register,
        )

        self.email_field.entry.focus_set()
        self.email_field.entry.bind("<Return>", lambda _event: self.submit())
        self.password_field.entry.bind("<Return>", lambda _event: self.submit())

    def _toggle_password(self) -> None:
        self.password_field.set_password_visible(
            self.password_visible.get()
        )

    def submit(self) -> None:
        email = self.email.get().strip()
        password = self.password.get()

        if not EMAIL_PATTERN.fullmatch(email):
            self.show_error("Enter a valid email address.")
            self.email_field.entry.focus_set()
            return

        if not password:
            self.show_error("Enter your password.")
            self.password_field.entry.focus_set()
            return

        if self._login_in_progress:
            return

        self._login_in_progress = True
        self.status_text.set("Signing in...")

        threading.Thread(
            target=self._perform_login,
            args=(email, password),
            daemon=True,
            name="fintrade-login",
        ).start()

        self.after(50, self._poll_login_result)

    def _perform_login(
        self,
        email: str,
        password: str,
    ) -> None:
        try:
            response = self.authentication_service.login(
                email,
                password,
            )
            self._login_results.put(("response", response))
        except (OSError, ConnectionError, ValueError) as exception:
            self._login_results.put(("error", exception))

    def _poll_login_result(self) -> None:
        try:
            result_type, result = self._login_results.get_nowait()
        except queue.Empty:
            self.after(50, self._poll_login_result)
            return

        self._login_in_progress = False

        if result_type == "error":
            self.show_error(
                "Could not connect to the FinTrade server."
            )
            return

        self._handle_login_response(result)

    def _handle_login_response(self, response: object) -> None:
        if not isinstance(response, dict):
            self.show_error("The server returned an invalid response.")
            return

        if response.get("status") == "OK":
            user = response.get("data") or {}
            username = user.get("username", "trader")
            self.show_success(f"Welcome back, {username}!")
            return

        error = response.get("error") or {}
        self.show_error(
            error.get("message", "The server rejected the login request.")
        )
