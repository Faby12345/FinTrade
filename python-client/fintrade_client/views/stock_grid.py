"""Reusable stock-card grid used by the dashboard."""

from __future__ import annotations

from decimal import Decimal, InvalidOperation
import tkinter as tk
from collections.abc import Callable
from typing import Any

from .. import theme


class StockGrid(tk.Frame):
    """Scrollable two-column grid of stocks."""

    def __init__(
        self,
        parent: tk.Widget,
        *,
        on_refresh: Callable[[], None],
    ) -> None:
        super().__init__(
            parent,
            background=theme.FIELD_BACKGROUND,
            highlightthickness=1,
            highlightbackground=theme.BORDER,
        )
        self._on_refresh = on_refresh

        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1)

        self._build_header()
        self._build_scroll_area()

    def _build_header(self) -> None:
        header = tk.Frame(
            self,
            background="#eef2ff",
            padx=18,
            pady=14,
        )
        header.grid(row=0, column=0, sticky="ew")
        header.grid_columnconfigure(0, weight=1)

        title_group = tk.Frame(header, background="#eef2ff")
        title_group.grid(row=0, column=0, sticky="w")

        tk.Label(
            title_group,
            text="Market",
            background="#eef2ff",
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 15, "bold"),
        ).pack(side="left")

        self._count_label = tk.Label(
            title_group,
            text="",
            background="#eef2ff",
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 10),
        )
        self._count_label.pack(side="left", padx=(9, 0), pady=(3, 0))

        refresh = tk.Label(
            header,
            text="Refresh",
            background="#eef2ff",
            foreground=theme.PRIMARY,
            cursor="hand2",
            font=(theme.FONT_FAMILY, 10, "bold"),
            padx=6,
            pady=3,
            takefocus=True,
        )
        refresh.grid(row=0, column=1, sticky="e")
        refresh.bind("<Button-1>", lambda _event: self._on_refresh())
        refresh.bind("<Return>", lambda _event: self._on_refresh())

    def _build_scroll_area(self) -> None:
        scroll_area = tk.Frame(self, background=theme.FIELD_BACKGROUND)
        scroll_area.grid(row=1, column=0, sticky="nsew")
        scroll_area.grid_columnconfigure(0, weight=1)
        scroll_area.grid_rowconfigure(0, weight=1)

        self._canvas = tk.Canvas(
            scroll_area,
            height=390,
            background=theme.FIELD_BACKGROUND,
            borderwidth=0,
            highlightthickness=0,
        )
        self._canvas.grid(row=0, column=0, sticky="nsew")

        scrollbar = tk.Scrollbar(
            scroll_area,
            orient="vertical",
            command=self._canvas.yview,
        )
        scrollbar.grid(row=0, column=1, sticky="ns")
        self._canvas.configure(yscrollcommand=scrollbar.set)

        self._content = tk.Frame(
            self._canvas,
            background=theme.FIELD_BACKGROUND,
            padx=12,
            pady=12,
        )
        self._content.grid_columnconfigure(0, weight=1, uniform="stock")
        self._content.grid_columnconfigure(1, weight=1, uniform="stock")

        self._content_window = self._canvas.create_window(
            (0, 0),
            window=self._content,
            anchor="nw",
        )
        self._content.bind("<Configure>", self._update_scroll_region)
        self._canvas.bind("<Configure>", self._resize_content)

    def show_loading(self) -> None:
        self._count_label.configure(text="Loading…")
        self._replace_content_message(
            "Loading market data…",
            theme.MUTED,
        )

    def show_error(self, message: str) -> None:
        self._count_label.configure(text="Unavailable")
        self._replace_content_message(message, theme.ERROR)

    def show_stocks(self, stocks: list[dict[str, Any]]) -> None:
        self._clear_content()
        self._count_label.configure(
            text=f"{len(stocks)} stock{'s' if len(stocks) != 1 else ''}"
        )

        if not stocks:
            self._replace_content_message(
                "No stocks are available yet.",
                theme.MUTED,
            )
            return

        for index, stock in enumerate(stocks):
            card = self._create_stock_card(stock)
            card.grid(
                row=index // 2,
                column=index % 2,
                sticky="nsew",
                padx=6,
                pady=6,
            )

        self._canvas.yview_moveto(0)

    def _create_stock_card(self, stock: dict[str, Any]) -> tk.Frame:
        card = tk.Frame(
            self._content,
            background="#f8fafc",
            highlightthickness=1,
            highlightbackground="#dbe4f0",
            padx=14,
            pady=12,
        )
        card.grid_columnconfigure(0, weight=1)

        symbol = str(stock.get("symbol", "—"))
        name = str(stock.get("name", "Unknown company"))
        stock_id = str(stock.get("id", "—"))
        price = self._format_price(stock.get("price"))

        tk.Label(
            card,
            text=symbol,
            background="#f8fafc",
            foreground=theme.PRIMARY,
            font=(theme.FONT_FAMILY, 13, "bold"),
        ).grid(row=0, column=0, sticky="w")

        tk.Label(
            card,
            text=price,
            background="#f8fafc",
            foreground=theme.SUCCESS,
            font=(theme.FONT_FAMILY, 13, "bold"),
        ).grid(row=0, column=1, sticky="e", padx=(8, 0))

        tk.Label(
            card,
            text=name,
            background="#f8fafc",
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 10, "bold"),
            anchor="w",
        ).grid(row=1, column=0, columnspan=2, sticky="ew", pady=(9, 8))

        tk.Label(
            card,
            text=f"ID  {stock_id}",
            background="#f8fafc",
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 8),
            anchor="w",
            justify="left",
            wraplength=210,
        ).grid(row=2, column=0, columnspan=2, sticky="ew")

        return card

    def _replace_content_message(self, message: str, color: str) -> None:
        self._clear_content()
        tk.Label(
            self._content,
            text=message,
            background=theme.FIELD_BACKGROUND,
            foreground=color,
            font=(theme.FONT_FAMILY, 11),
            wraplength=430,
            justify="center",
            pady=45,
        ).grid(row=0, column=0, columnspan=2, sticky="ew")

    def _clear_content(self) -> None:
        for child in self._content.winfo_children():
            child.destroy()

    def _update_scroll_region(self, _event: tk.Event) -> None:
        self._canvas.configure(scrollregion=self._canvas.bbox("all"))

    def _resize_content(self, event: tk.Event) -> None:
        self._canvas.itemconfigure(
            self._content_window,
            width=event.width,
        )

    @staticmethod
    def _format_price(value: object) -> str:
        try:
            return f"${Decimal(str(value)):,.2f}"
        except (InvalidOperation, TypeError, ValueError):
            return "—"
