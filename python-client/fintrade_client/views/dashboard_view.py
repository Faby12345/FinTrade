"""Main page shown after a successful login."""

from __future__ import annotations

import tkinter as tk
from typing import Any

from .. import theme
from ..services.stock_service import StockService
from .stock_grid import StockGrid


class DashboardView(tk.Frame):
    """Minimal authenticated dashboard placeholder."""

    def __init__(
        self,
        parent: tk.Widget,
        *,
        user: dict[str, object],
        stock_service: StockService,
    ) -> None:
        super().__init__(
            parent,
            background=theme.CARD_BACKGROUND,
            highlightthickness=1,
            highlightbackground="#293555",
            padx=38,
            pady=38,
        )
        self._stock_service = stock_service

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
        ).pack(anchor="w", pady=(10, 22))

        self._stock_grid = StockGrid(
            self,
            on_refresh=self._load_stocks,
        )
        self._stock_grid.pack(fill="both", expand=True)

        self.after(50, self._load_stocks)

    def _load_stocks(self) -> None:
        self._stock_grid.show_loading()

        try:
            response = self._stock_service.list_stocks()
        except (ConnectionError, OSError, ValueError) as exception:
            self._stock_grid.show_error(str(exception))
            return

        if response.get("status") != "OK":
            error = response.get("error")
            message = "The server could not load market data."

            if isinstance(error, dict):
                message = str(error.get("message", message))

            self._stock_grid.show_error(message)
            return

        stocks = response.get("data")

        if not isinstance(stocks, list):
            self._stock_grid.show_error(
                "The server returned invalid market data."
            )
            return

        valid_stocks: list[dict[str, Any]] = [
            stock for stock in stocks if isinstance(stock, dict)
        ]
        self._stock_grid.show_stocks(valid_stocks)
