"""Login screen with local-only validation."""

from __future__ import annotations

import re
import tkinter as tk
from collections.abc import Callable

from .. import theme
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
    ) -> None:
        super().__init__(
            parent,
            on_login=on_login,
            on_register=on_register,
        )

        self.email = tk.StringVar()
        self.password = tk.StringVar()
        self.password_visible = tk.BooleanVar(value=False)

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

        self.show_success(
            "Details validated locally. Server login is not connected yet."
        )
