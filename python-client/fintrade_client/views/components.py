"""Reusable Tkinter controls for authentication screens."""

from __future__ import annotations

import tkinter as tk
from collections.abc import Callable

from .. import theme


class FormField(tk.Frame):
    """A visible label and bordered text input."""

    def __init__(
        self,
        parent: tk.Widget,
        label: str,
        variable: tk.StringVar,
        *,
        password: bool = False,
    ) -> None:
        super().__init__(parent, background=theme.CARD_BACKGROUND)
        self._password = password

        tk.Label(
            self,
            text=label,
            background=theme.CARD_BACKGROUND,
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 10, "bold"),
        ).pack(anchor="w", pady=(0, 7))

        border = tk.Frame(
            self,
            background=theme.BORDER,
            padx=1,
            pady=1,
        )
        border.pack(fill="x")

        field_surface = tk.Frame(
            border,
            background=theme.FIELD_BACKGROUND,
        )
        field_surface.pack(fill="x")

        self.entry = tk.Entry(
            field_surface,
            textvariable=variable,
            show="•" if password else "",
            relief="flat",
            borderwidth=0,
            highlightthickness=0,
            background=theme.FIELD_BACKGROUND,
            foreground=theme.TEXT,
            insertbackground=theme.TEXT,
            selectbackground=theme.PRIMARY,
            selectforeground="white",
            font=(theme.FONT_FAMILY, 11),
        )
        self.entry.pack(fill="x", ipady=9, padx=11)

    def set_password_visible(self, visible: bool) -> None:
        if self._password:
            self.entry.configure(show="" if visible else "•")


class PrimaryButton(tk.Label):
    def __init__(
        self,
        parent: tk.Widget,
        text: str,
        command: Callable[[], None],
    ) -> None:
        self._command = command
        super().__init__(
            parent,
            text=text,
            relief="flat",
            borderwidth=0,
            background=theme.PRIMARY,
            foreground="white",
            cursor="hand2",
            font=(theme.FONT_FAMILY, 11, "bold"),
            pady=11,
            takefocus=True,
        )
        self.bind("<Button-1>", self._activate)
        self.bind("<Return>", self._activate)
        self.bind("<space>", self._activate)
        self.bind(
            "<Enter>",
            lambda _event: self.configure(background=theme.PRIMARY_HOVER),
        )
        self.bind(
            "<Leave>",
            lambda _event: self.configure(background=theme.PRIMARY),
        )

    def _activate(self, _event: tk.Event) -> None:
        self._command()


class LinkButton(tk.Label):
    def __init__(
        self,
        parent: tk.Widget,
        text: str,
        command: Callable[[], None],
    ) -> None:
        self._command = command
        super().__init__(
            parent,
            text=text,
            relief="flat",
            borderwidth=0,
            background=theme.CARD_BACKGROUND,
            foreground=theme.PRIMARY,
            cursor="hand2",
            font=(theme.FONT_FAMILY, 10, "bold"),
            padx=4,
            pady=2,
            takefocus=True,
        )
        self.bind("<Button-1>", self._activate)
        self.bind("<Return>", self._activate)
        self.bind("<space>", self._activate)
        self.bind(
            "<Enter>",
            lambda _event: self.configure(foreground=theme.PRIMARY_HOVER),
        )
        self.bind(
            "<Leave>",
            lambda _event: self.configure(foreground=theme.PRIMARY),
        )

    def _activate(self, _event: tk.Event) -> None:
        self._command()
