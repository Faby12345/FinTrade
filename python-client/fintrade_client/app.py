"""Application window and navigation for the FinTrade client."""

from __future__ import annotations

import tkinter as tk

from . import theme
from .network.trading_client import TradingClient
from .services.account_service import AccountService
from .services.authentication_service import AuthenticationService
from .services.stock_service import StockService
from .views.dashboard_view import DashboardView
from .views.login_view import LoginView
from .views.register_view import RegisterView


class FinTradeApp:
    """Owns the root window and swaps application views."""

    def __init__(self) -> None:
        self.root = tk.Tk()
        self.root.title("FinTrade")
        self.root.geometry("1080x700")
        self.root.minsize(900, 640)
        self.root.configure(background=theme.WINDOW_BACKGROUND)

        self.trading_client = TradingClient(
            host="127.0.0.1",
            port=9090,
        )
        self.authentication_service = AuthenticationService(
            self.trading_client
        )
        self.account_service = AccountService(self.trading_client)
        self.stock_service = StockService(self.trading_client)
        self.root.protocol("WM_DELETE_WINDOW", self._close)

        self._current_view: tk.Frame | None = None
        self._build_shell()
        self.show_login()

    def run(self) -> None:
        self.root.mainloop()

    def _build_shell(self) -> None:
        self.root.grid_rowconfigure(0, weight=1)
        self.root.grid_columnconfigure(0, weight=4, minsize=330)
        self.root.grid_columnconfigure(1, weight=6, minsize=570)

        self._build_brand_panel()

        self.content_panel = tk.Frame(
            self.root,
            background=theme.WINDOW_BACKGROUND,
        )
        self.content_panel.grid(row=0, column=1, sticky="nsew")
        self.content_panel.grid_rowconfigure(0, weight=1)
        self.content_panel.grid_columnconfigure(0, weight=1)

    def _build_brand_panel(self) -> None:
        panel = tk.Frame(
            self.root,
            background=theme.BRAND_BACKGROUND,
            padx=50,
        )
        panel.grid(row=0, column=0, sticky="nsew")
        panel.grid_rowconfigure(0, weight=1)
        panel.grid_rowconfigure(2, weight=1)
        panel.grid_columnconfigure(0, weight=1)

        content = tk.Frame(panel, background=theme.BRAND_BACKGROUND)
        content.grid(row=1, column=0, sticky="ew")

        tk.Label(
            content,
            text="FT",
            width=3,
            background=theme.PRIMARY,
            foreground="white",
            font=(theme.FONT_FAMILY, 22, "bold"),
            padx=4,
            pady=9,
        ).grid(row=0, column=0, sticky="w", pady=(0, 26))

        accent_line = tk.Frame(
            content,
            width=48,
            height=4,
            background=theme.ACCENT,
        )
        accent_line.grid(row=1, column=0, sticky="w", pady=(0, 18))

        tk.Label(
            content,
            text="FinTrade",
            background=theme.BRAND_BACKGROUND,
            foreground="white",
            font=(theme.FONT_FAMILY, 34, "bold"),
        ).grid(row=2, column=0, sticky="w")

        tk.Label(
            content,
            text="Practice trading without risking real money.",
            wraplength=280,
            justify="left",
            background=theme.BRAND_BACKGROUND,
            foreground="#bfdbfe",
            font=(theme.FONT_FAMILY, 17),
        ).grid(row=3, column=0, sticky="w", pady=(14, 22))

        tk.Label(
            content,
            text=(
                "Explore an exchange simulator with transparent "
                "orders, portfolios, and market activity."
            ),
            wraplength=280,
            justify="left",
            background=theme.BRAND_BACKGROUND,
            foreground="#94a3b8",
            font=(theme.FONT_FAMILY, 11),
        ).grid(row=4, column=0, sticky="w")

    def _show(self, view_type: type[tk.Frame]) -> None:
        if self._current_view is not None:
            self._current_view.destroy()

        self._current_view = view_type(
            self.content_panel,
            on_login=self.show_login,
            on_register=self.show_register,
            on_authenticated=self.show_dashboard,
            authentication_service=self.authentication_service,
        )
        self._current_view.grid(
            row=0,
            column=0,
            padx=54,
            pady=30,
        )

    def show_login(self) -> None:
        self._show(LoginView)

    def show_register(self) -> None:
        self._show(RegisterView)

    def show_dashboard(self, user: dict[str, object]) -> None:
        if self._current_view is not None:
            self._current_view.destroy()

        self._current_view = DashboardView(
            self.content_panel,
            user=user,
            account_service=self.account_service,
            stock_service=self.stock_service,
        )
        self._current_view.grid(
            row=0,
            column=0,
            padx=36,
            pady=30,
            sticky="nsew",
        )

    def _close(self) -> None:
        self.trading_client.close()
        self.root.destroy()
