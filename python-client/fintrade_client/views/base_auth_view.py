"""Common layout behavior for login and registration cards."""

from __future__ import annotations

import tkinter as tk
from collections.abc import Callable

from .. import theme
from .components import LinkButton, PrimaryButton


class BaseAuthView(tk.Frame):
    def __init__(
        self,
        parent: tk.Widget,
        *,
        on_login: Callable[[], None],
        on_register: Callable[[], None],
    ) -> None:
        super().__init__(
            parent,
            width=460,
            background=theme.CARD_BACKGROUND,
            highlightthickness=1,
            highlightbackground="#293555",
            padx=38,
            pady=25,
        )
        self.on_login = on_login
        self.on_register = on_register
        self.status_text = tk.StringVar()
        self.status_label: tk.Label | None = None

    def add_heading(self, title: str, subtitle: str) -> None:
        tk.Label(
            self,
            text=title,
            background=theme.CARD_BACKGROUND,
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 23, "bold"),
        ).pack(anchor="w")

        tk.Label(
            self,
            text=subtitle,
            wraplength=370,
            justify="left",
            background=theme.CARD_BACKGROUND,
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 11),
        ).pack(anchor="w", pady=(7, 19))

    def add_primary_button(
        self,
        text: str,
        command: Callable[[], None],
    ) -> None:
        PrimaryButton(self, text, command).pack(
            fill="x",
            pady=(14, 0),
        )

    def add_status(self) -> None:
        self.status_label = tk.Label(
            self,
            textvariable=self.status_text,
            wraplength=370,
            justify="left",
            background=theme.CARD_BACKGROUND,
            foreground=theme.ERROR,
            font=(theme.FONT_FAMILY, 10),
        )
        self.status_label.pack(anchor="w", pady=(8, 0))

    def add_navigation(
        self,
        prompt: str,
        action: str,
        command: Callable[[], None],
    ) -> None:
        row = tk.Frame(self, background=theme.CARD_BACKGROUND)
        row.pack(pady=(20, 0))

        tk.Label(
            row,
            text=prompt,
            background=theme.CARD_BACKGROUND,
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 10),
        ).pack(side="left")
        LinkButton(row, action, command).pack(side="left")

    def show_error(self, message: str) -> None:
        self.status_text.set(message)
        if self.status_label is not None:
            self.status_label.configure(foreground=theme.ERROR)

    def show_success(self, message: str) -> None:
        self.status_text.set(message)
        if self.status_label is not None:
            self.status_label.configure(foreground=theme.SUCCESS)
