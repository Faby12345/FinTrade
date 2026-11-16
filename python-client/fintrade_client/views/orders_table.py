"""Table displaying the authenticated user's orders."""

from __future__ import annotations

from decimal import Decimal, InvalidOperation
import tkinter as tk
from tkinter import ttk
from collections.abc import Callable
from typing import Any

from .. import theme


class OrdersTable(tk.Frame):
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
        self._build_table()

    def _build_header(self) -> None:
        header = tk.Frame(
            self,
            background="#eef2ff",
            padx=14,
            pady=10,
        )
        header.grid(row=0, column=0, sticky="ew")
        header.grid_columnconfigure(0, weight=1)

        title_group = tk.Frame(header, background="#eef2ff")
        title_group.grid(row=0, column=0, sticky="w")

        tk.Label(
            title_group,
            text="My orders",
            background="#eef2ff",
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 13, "bold"),
        ).pack(side="left")

        self._count_label = tk.Label(
            title_group,
            text="",
            background="#eef2ff",
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 9),
        )
        self._count_label.pack(side="left", padx=(8, 0))

        refresh = tk.Label(
            header,
            text="Refresh",
            background="#eef2ff",
            foreground=theme.PRIMARY,
            cursor="hand2",
            font=(theme.FONT_FAMILY, 9, "bold"),
            takefocus=True,
        )
        refresh.grid(row=0, column=1, sticky="e")
        refresh.bind("<Button-1>", lambda _event: self._on_refresh())
        refresh.bind("<Return>", lambda _event: self._on_refresh())

    def _build_table(self) -> None:
        style = ttk.Style(self)
        style.configure(
            "FinTrade.Treeview",
            background=theme.FIELD_BACKGROUND,
            fieldbackground=theme.FIELD_BACKGROUND,
            foreground=theme.TEXT,
            rowheight=25,
            font=(theme.FONT_FAMILY, 9),
        )
        style.configure(
            "FinTrade.Treeview.Heading",
            background="#f1f5f9",
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 8, "bold"),
        )

        table_area = tk.Frame(self, background=theme.FIELD_BACKGROUND)
        table_area.grid(row=1, column=0, sticky="nsew")
        table_area.grid_columnconfigure(0, weight=1)
        table_area.grid_rowconfigure(0, weight=1)

        columns = (
            "symbol",
            "side",
            "quantity",
            "filled",
            "remaining",
            "price",
            "status",
        )
        self._tree = ttk.Treeview(
            table_area,
            columns=columns,
            show="headings",
            height=5,
            style="FinTrade.Treeview",
        )
        self._tree.grid(row=0, column=0, sticky="nsew")

        headings = {
            "symbol": "Stock",
            "side": "Side",
            "quantity": "Qty",
            "filled": "Filled",
            "remaining": "Left",
            "price": "Limit",
            "status": "Status",
        }
        widths = {
            "symbol": 70,
            "side": 55,
            "quantity": 48,
            "filled": 55,
            "remaining": 48,
            "price": 85,
            "status": 120,
        }

        for column in columns:
            self._tree.heading(column, text=headings[column])
            self._tree.column(
                column,
                width=widths[column],
                minwidth=40,
                anchor="center",
                stretch=column in {"symbol", "status"},
            )

        self._tree.tag_configure("BUY", foreground=theme.SUCCESS)
        self._tree.tag_configure("SELL", foreground=theme.ERROR)

        scrollbar = tk.Scrollbar(
            table_area,
            orient="vertical",
            command=self._tree.yview,
        )
        scrollbar.grid(row=0, column=1, sticky="ns")
        self._tree.configure(yscrollcommand=scrollbar.set)

        self._message = tk.Label(
            self,
            text="",
            background=theme.FIELD_BACKGROUND,
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 9),
        )
        self._message.grid(row=2, column=0, sticky="w", padx=12, pady=5)

    def show_loading(self) -> None:
        self._clear()
        self._count_label.configure(text="Loading…")
        self._message.configure(text="Loading your orders…", foreground=theme.MUTED)

    def show_error(self, message: str) -> None:
        self._clear()
        self._count_label.configure(text="Unavailable")
        self._message.configure(text=message, foreground=theme.ERROR)

    def show_orders(self, orders: list[dict[str, Any]]) -> None:
        self._clear()
        self._count_label.configure(
            text=f"{len(orders)} order{'s' if len(orders) != 1 else ''}"
        )

        if not orders:
            self._message.configure(
                text="You have not placed any orders yet.",
                foreground=theme.MUTED,
            )
            return

        self._message.configure(text="")

        for order in orders:
            side = str(order.get("side", "—"))
            self._tree.insert(
                "",
                "end",
                values=(
                    order.get("symbol", "—"),
                    side,
                    order.get("originalQuantity", "—"),
                    order.get("filledQuantity", "—"),
                    order.get("remainingQuantity", "—"),
                    self._format_money(order.get("limitPrice")),
                    self._format_status(order.get("status")),
                ),
                tags=(side,),
            )

    def _clear(self) -> None:
        for item in self._tree.get_children():
            self._tree.delete(item)

    @staticmethod
    def _format_money(value: object) -> str:
        try:
            return f"${Decimal(str(value)):,.2f}"
        except (InvalidOperation, TypeError, ValueError):
            return "—"

    @staticmethod
    def _format_status(value: object) -> str:
        return str(value or "—").replace("_", " ").title()
