"""Authenticated FinTrade workspace with sidebar navigation."""

from __future__ import annotations

from collections.abc import Callable
from decimal import Decimal, InvalidOperation
import tkinter as tk
from typing import Any

from .. import theme
from ..services.account_service import AccountService
from ..services.order_service import OrderService
from ..services.position_service import PositionService
from ..services.stock_service import StockService
from .orders_table import OrdersTable
from .positions_table import PositionsTable
from .stock_grid import StockGrid


class DashboardView(tk.Frame):
    def __init__(
        self,
        parent: tk.Widget,
        *,
        user: dict[str, object],
        account_service: AccountService,
        order_service: OrderService,
        position_service: PositionService,
        stock_service: StockService,
    ) -> None:
        super().__init__(parent, background=theme.WINDOW_BACKGROUND)

        self._account_service = account_service
        self._order_service = order_service
        self._position_service = position_service
        self._stock_service = stock_service
        self._loaded_pages: set[str] = set()
        self._nav_buttons: dict[str, tk.Button] = {}

        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(1, weight=1)

        username = str(user.get("username", "trader"))
        self._build_sidebar(username)
        self._build_workspace(username)

        self._show_page("positions")
        self.after(50, self._load_account)

    def _build_sidebar(self, username: str) -> None:
        sidebar = tk.Frame(
            self,
            width=220,
            background=theme.BRAND_BACKGROUND,
            padx=22,
            pady=26,
        )
        sidebar.grid(row=0, column=0, sticky="ns")
        sidebar.grid_propagate(False)
        sidebar.grid_columnconfigure(0, weight=1)
        sidebar.grid_rowconfigure(5, weight=1)

        brand = tk.Frame(sidebar, background=theme.BRAND_BACKGROUND)
        brand.grid(row=0, column=0, sticky="ew", pady=(0, 38))
        tk.Label(
            brand,
            text="FT",
            width=3,
            background=theme.PRIMARY,
            foreground="white",
            font=(theme.FONT_FAMILY, 15, "bold"),
            padx=2,
            pady=6,
        ).pack(side="left")
        tk.Label(
            brand,
            text="FinTrade",
            background=theme.BRAND_BACKGROUND,
            foreground="white",
            font=(theme.FONT_FAMILY, 17, "bold"),
        ).pack(side="left", padx=(10, 0))

        tk.Label(
            sidebar,
            text="WORKSPACE",
            background=theme.BRAND_BACKGROUND,
            foreground="#64748b",
            font=(theme.FONT_FAMILY, 8, "bold"),
        ).grid(row=1, column=0, sticky="w", pady=(0, 9))

        pages = (
            ("positions", "My positions"),
            ("orders", "My orders"),
            ("market", "Market"),
        )
        for row, (page, title) in enumerate(pages, start=2):
            button = tk.Button(
                sidebar,
                text=title,
                command=lambda selected=page: self._show_page(selected),
                anchor="w",
                relief="flat",
                borderwidth=0,
                highlightthickness=0,
                background=theme.BRAND_BACKGROUND,
                activebackground="#1e293b",
                foreground="#cbd5e1",
                activeforeground="white",
                font=(theme.FONT_FAMILY, 11, "bold"),
                padx=12,
                pady=11,
                cursor="hand2",
            )
            button.grid(row=row, column=0, sticky="ew", pady=3)
            self._nav_buttons[page] = button

        user_card = tk.Frame(
            sidebar,
            background="#1e293b",
            padx=12,
            pady=11,
        )
        user_card.grid(row=6, column=0, sticky="sew", pady=(34, 0))
        tk.Label(
            user_card,
            text="Signed in as",
            background="#1e293b",
            foreground="#94a3b8",
            font=(theme.FONT_FAMILY, 8),
        ).pack(anchor="w")
        tk.Label(
            user_card,
            text=username,
            background="#1e293b",
            foreground="white",
            font=(theme.FONT_FAMILY, 10, "bold"),
        ).pack(anchor="w", pady=(3, 0))

    def _build_workspace(self, username: str) -> None:
        workspace = tk.Frame(
            self,
            background=theme.CARD_BACKGROUND,
            padx=30,
            pady=24,
        )
        workspace.grid(row=0, column=1, sticky="nsew")
        workspace.grid_columnconfigure(0, weight=1)
        workspace.grid_rowconfigure(3, weight=1)

        tk.Label(
            workspace,
            text="Dashboard",
            background=theme.CARD_BACKGROUND,
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 25, "bold"),
        ).grid(row=0, column=0, sticky="w")
        tk.Label(
            workspace,
            text=f"Welcome back, {username}.",
            background=theme.CARD_BACKGROUND,
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 11),
        ).grid(row=1, column=0, sticky="w", pady=(6, 16))

        self._build_account_summary(workspace)

        self._page_container = tk.Frame(
            workspace,
            background=theme.CARD_BACKGROUND,
        )
        self._page_container.grid(row=3, column=0, sticky="nsew")
        self._page_container.grid_rowconfigure(0, weight=1)
        self._page_container.grid_columnconfigure(0, weight=1)

        self._positions_table = PositionsTable(
            self._page_container,
            on_refresh=self._load_positions,
        )
        self._orders_table = OrdersTable(
            self._page_container,
            on_refresh=self._load_orders,
        )
        self._stock_grid = StockGrid(
            self._page_container,
            on_refresh=self._load_stocks,
        )
        self._pages: dict[str, tk.Frame] = {
            "positions": self._positions_table,
            "orders": self._orders_table,
            "market": self._stock_grid,
        }
        for page in self._pages.values():
            page.grid(row=0, column=0, sticky="nsew")

    def _build_account_summary(self, parent: tk.Widget) -> None:
        summary = tk.Frame(
            parent,
            background="#eef2ff",
            highlightthickness=1,
            highlightbackground=theme.BORDER,
            padx=18,
            pady=12,
        )
        summary.grid(row=2, column=0, sticky="ew", pady=(0, 18))
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
            value_label.grid(row=1, column=column, sticky="w", pady=(4, 0))
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
            pady=(5, 0),
        )

    def _show_page(self, page_name: str) -> None:
        self._pages[page_name].tkraise()
        for name, button in self._nav_buttons.items():
            active = name == page_name
            button.configure(
                background=theme.PRIMARY if active else theme.BRAND_BACKGROUND,
                foreground="white" if active else "#cbd5e1",
            )

        if page_name not in self._loaded_pages:
            loaders = {
                "positions": self._load_positions,
                "orders": self._load_orders,
                "market": self._load_stocks,
            }
            self.after_idle(loaders[page_name])

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
            self._show_account_error("The server returned invalid account data.")
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

    def _load_positions(self) -> None:
        self._positions_table.show_loading()
        try:
            response = self._position_service.list_my_positions()
        except (ConnectionError, OSError, ValueError) as exception:
            self._positions_table.show_error(str(exception))
            return

        positions = self._response_list(
            response,
            "The server could not load your positions.",
            self._positions_table.show_error,
        )
        if positions is not None:
            self._loaded_pages.add("positions")
            self._positions_table.show_positions(positions)

    def _load_orders(self) -> None:
        self._orders_table.show_loading()
        try:
            response = self._order_service.list_my_orders()
        except (ConnectionError, OSError, ValueError) as exception:
            self._orders_table.show_error(str(exception))
            return

        orders = self._response_list(
            response,
            "The server could not load your orders.",
            self._orders_table.show_error,
        )
        if orders is not None:
            self._loaded_pages.add("orders")
            self._orders_table.show_orders(orders)

    def _load_stocks(self) -> None:
        self._stock_grid.show_loading()
        try:
            response = self._stock_service.list_stocks()
        except (ConnectionError, OSError, ValueError) as exception:
            self._stock_grid.show_error(str(exception))
            return

        stocks = self._response_list(
            response,
            "The server could not load market data.",
            self._stock_grid.show_error,
        )
        if stocks is not None:
            self._loaded_pages.add("market")
            self._stock_grid.show_stocks(stocks)

    @staticmethod
    def _response_list(
            response: dict[str, Any],
            default_error: str,
            show_error: Callable[[str], None],
    ) -> list[dict[str, Any]] | None:
        if response.get("status") != "OK":
            error = response.get("error")
            message = default_error
            if isinstance(error, dict):
                message = str(error.get("message", message))
            show_error(message)
            return None

        data = response.get("data")
        if not isinstance(data, list):
            show_error("The server returned invalid list data.")
            return None
        return [item for item in data if isinstance(item, dict)]

    @staticmethod
    def _format_money(value: object) -> str:
        try:
            return f"${Decimal(str(value)):,.2f}"
        except (InvalidOperation, TypeError, ValueError):
            return "—"
