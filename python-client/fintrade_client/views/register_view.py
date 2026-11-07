"""Registration screen with local-only validation."""

from __future__ import annotations

import re
import tkinter as tk
from collections.abc import Callable

from .. import theme
from .base_auth_view import BaseAuthView
from .components import FormField, LinkButton


EMAIL_PATTERN = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")


class RegisterView(BaseAuthView):
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

        self.username = tk.StringVar()
        self.email = tk.StringVar()
        self.password = tk.StringVar()
        self.confirm_password = tk.StringVar()
        self.accept_simulation = tk.BooleanVar(value=False)

        LinkButton(
            self,
            "← Back to sign in",
            self.on_login,
        ).pack(anchor="w", pady=(0, 10))

        self.add_heading(
            "Create an account",
            "Set up your profile for the simulated FinTrade exchange.",
        )

        self.username_field = self._add_field("Username", self.username)
        self.email_field = self._add_field("Email", self.email)
        self.password_field = self._add_field(
            "Password",
            self.password,
            password=True,
        )
        self.confirm_field = self._add_field(
            "Confirm password",
            self.confirm_password,
            password=True,
            bottom_padding=0,
        )

        tk.Checkbutton(
            self,
            text="I understand that all funds and trades are simulated",
            variable=self.accept_simulation,
            wraplength=360,
            justify="left",
            background=theme.CARD_BACKGROUND,
            activebackground=theme.CARD_BACKGROUND,
            foreground=theme.MUTED,
            activeforeground=theme.TEXT,
            selectcolor=theme.CARD_BACKGROUND,
            font=(theme.FONT_FAMILY, 9),
        ).pack(anchor="w", pady=(10, 0))

        self.add_primary_button("Create account", self.submit)
        self.add_status()

        self.username_field.entry.focus_set()
        for field in (
            self.username_field,
            self.email_field,
            self.password_field,
            self.confirm_field,
        ):
            field.entry.bind("<Return>", lambda _event: self.submit())

    def _add_field(
        self,
        label: str,
        variable: tk.StringVar,
        *,
        password: bool = False,
        bottom_padding: int = 10,
    ) -> FormField:
        field = FormField(
            self,
            label,
            variable,
            password=password,
        )
        field.pack(fill="x", pady=(0, bottom_padding))
        return field

    def submit(self) -> None:
        username = self.username.get().strip()
        email = self.email.get().strip()
        password = self.password.get()

        if len(username) < 3:
            self.show_error("Username must contain at least 3 characters.")
            self.username_field.entry.focus_set()
            return

        if not EMAIL_PATTERN.fullmatch(email):
            self.show_error("Enter a valid email address.")
            self.email_field.entry.focus_set()
            return

        if len(password) < 8:
            self.show_error("Password must contain at least 8 characters.")
            self.password_field.entry.focus_set()
            return

        if password != self.confirm_password.get():
            self.show_error("The passwords do not match.")
            self.confirm_field.entry.focus_set()
            return

        if not self.accept_simulation.get():
            self.show_error("Confirm that you understand this is a simulation.")
            return

        self.show_success(
            "Details validated locally. Server registration is not connected yet."
        )
