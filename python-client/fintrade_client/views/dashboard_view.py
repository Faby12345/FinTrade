"""Main page shown after a successful login."""

from __future__ import annotations

import tkinter as tk
from decimal import Decimal, InvalidOperation
from typing import Any

from .. import theme
from ..services.account_service import AccountService
from ..services.stock_service import StockService
from .stock_grid import StockGrid


class DashboardView(tk.Frame):
    """Minimal authenticated dashboard placeholder."""

    def __init__(
        self,
        parent: tk.Widget,
        *,
        user: dict[str, object],
        account_service: AccountService,
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
        self._account_service = account_service
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
        ).pack(anchor="w", pady=(10, 18))

        self._build_account_summary()

        self._stock_grid = StockGrid(
            self,
            on_refresh=self._load_stocks,
        )
        self._stock_grid.pack(fill="both", expand=True)

        self.after(50, self._load_dashboard)

    def _build_account_summary(self) -> None:
        summary = tk.Frame(
            self,
            background="#eef2ff",
            highlightthickness=1,
            highlightbackground=theme.BORDER,
            padx=18,
            pady=14,
        )
        summary.pack(fill="x", pady=(0, 18))

        for column in range(3):
            summary.grid_columnconfigure(column, weight=1)

        self._account_values: dict[str, tk.Label] = {}
        fields = (
            ("cashBalance", "Cash balance"),
            ("reservedCash", "Reserved cash"),
            ("availableCash", "Available cash"),
        )

        for column, (field, title) in enumerate(fields):
            tk.Label(
                summary,
                text=title,
                background="#eef2ff",
                foreground=theme.MUTED,
                font=(theme.FONT_FAMILY, 9, "bold"),
            ).grid(row=0, column=column, sticky="w")

            value_label = tk.Label(
                summary,
                text="Loading…",
                background="#eef2ff",
                foreground=theme.TEXT,
                font=(theme.FONT_FAMILY, 16, "bold"),
            )
            value_label.grid(
                row=1,
                column=column,
                sticky="w",
                pady=(5, 0),
            )
            self._account_values[field] = value_label

        self._account_error = tk.Label(
            summary,
            text="",
            background="#eef2ff",
            foreground=theme.ERROR,
            font=(theme.FONT_FAMILY, 9),
        )
        self._account_error.grid(
            row=2,
            column=0,
            columnspan=3,
            sticky="w",
            pady=(7, 0),
        )

    def _load_dashboard(self) -> None:
        self._load_account()
        self._load_stocks()

    def _load_account(self) -> None:
        try:
            response = self._account_service.get_account()
        except (ConnectionError, OSError, ValueError) as exception:
            self._show_account_error(str(exception))
            return

        if response.get("status") != "OK":
            error = response.get("error")
            message = "The server could not load your account."

            if isinstance(error, dict):
                message = str(error.get("message", message))

            self._show_account_error(message)
            return

        account = response.get("data")

        if not isinstance(account, dict):
            self._show_account_error(
                "The server returned invalid account data."
            )
            return

        self._account_error.configure(text="")
        for field, label in self._account_values.items():
            label.configure(
                text=self._format_money(account.get(field)),
                foreground=theme.TEXT,
            )

    def _show_account_error(self, message: str) -> None:
        for label in self._account_values.values():
            label.configure(text="—", foreground=theme.MUTED)

        self._account_error.configure(text=message)

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

    @staticmethod
    def _format_money(value: object) -> str:
        try:
            return f"${Decimal(str(value)):,.2f}"
        except (InvalidOperation, TypeError, ValueError):
            return "—"
