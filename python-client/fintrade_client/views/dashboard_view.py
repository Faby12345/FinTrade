"""Main page shown after a successful login."""

from __future__ import annotations

import tkinter as tk

from .. import theme


class DashboardView(tk.Frame):
    """Minimal authenticated dashboard placeholder."""

    def __init__(
        self,
        parent: tk.Widget,
        *,
        user: dict[str, object],
    ) -> None:
        super().__init__(
            parent,
            width=460,
            background=theme.CARD_BACKGROUND,
            highlightthickness=1,
            highlightbackground="#293555",
            padx=38,
            pady=38,
        )

        username = user.get("username", "trader")

        tk.Label(
            self,
            text="Dashboard",
            background=theme.CARD_BACKGROUND,
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 28, "bold"),
        ).pack(anchor="w")

        tk.Label(
            self,
            text=f"Welcome, {username}!",
            background=theme.CARD_BACKGROUND,
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 13),
        ).pack(anchor="w", pady=(10, 0))
