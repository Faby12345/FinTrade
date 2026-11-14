"""Table displaying the authenticated user's stock positions."""

from __future__ import annotations

from collections.abc import Callable
from decimal import Decimal, InvalidOperation
import tkinter as tk
from tkinter import ttk
from typing import Any

from .. import theme


class PositionsTable(tk.Frame):
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
        header = tk.Frame(self, background="#eef2ff", padx=18, pady=14)
        header.grid(row=0, column=0, sticky="ew")
        header.grid_columnconfigure(0, weight=1)

        title_group = tk.Frame(header, background="#eef2ff")
        title_group.grid(row=0, column=0, sticky="w")
        tk.Label(
            title_group,
            text="My positions",
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

    def _build_table(self) -> None:
        style = ttk.Style(self)
        style.configure(
            "FinTradePositions.Treeview",
            background=theme.FIELD_BACKGROUND,
            fieldbackground=theme.FIELD_BACKGROUND,
            foreground=theme.TEXT,
            rowheight=30,
            font=(theme.FONT_FAMILY, 10),
        )
        style.configure(
            "FinTradePositions.Treeview.Heading",
            background="#f1f5f9",
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 9, "bold"),
        )

        table_area = tk.Frame(self, background=theme.FIELD_BACKGROUND)
        table_area.grid(row=1, column=0, sticky="nsew")
        table_area.grid_columnconfigure(0, weight=1)
        table_area.grid_rowconfigure(0, weight=1)

        columns = (
            "symbol", "name", "quantity", "average",
            "current", "value", "profit",
        )
        self._tree = ttk.Treeview(
            table_area,
            columns=columns,
            show="headings",
            style="FinTradePositions.Treeview",
        )
        self._tree.grid(row=0, column=0, sticky="nsew")

        headings = {
            "symbol": "Stock", "name": "Company", "quantity": "Quantity",
            "average": "Average price", "current": "Current price",
            "value": "Market value", "profit": "Profit / loss",
        }
        widths = {
            "symbol": 75, "name": 180, "quantity": 75,
            "average": 100, "current": 100, "value": 105, "profit": 105,
        }
        for column in columns:
            self._tree.heading(column, text=headings[column])
            self._tree.column(
                column,
                width=widths[column],
                minwidth=55,
                anchor="w" if column == "name" else "center",
                stretch=column == "name",
            )

        self._tree.tag_configure("profit", foreground=theme.SUCCESS)
        self._tree.tag_configure("loss", foreground=theme.ERROR)
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
            font=(theme.FONT_FAMILY, 10),
        )
        self._message.grid(row=2, column=0, sticky="w", padx=14, pady=7)

    def show_loading(self) -> None:
        self._clear()
        self._count_label.configure(text="Loading…")
        self._message.configure(
            text="Loading your positions…",
            foreground=theme.MUTED,
        )

    def show_error(self, message: str) -> None:
        self._clear()
        self._count_label.configure(text="Unavailable")
        self._message.configure(text=message, foreground=theme.ERROR)

    def show_positions(self, positions: list[dict[str, Any]]) -> None:
        self._clear()
        suffix = "s" if len(positions) != 1 else ""
        self._count_label.configure(
            text=f"{len(positions)} position{suffix}"
        )
        if not positions:
            self._message.configure(
                text="You do not own any stock positions yet.",
                foreground=theme.MUTED,
            )
            return

        self._message.configure(text="")
        for position in positions:
            profit = self._decimal(position.get("unrealizedProfitLoss"))
            tag = "loss" if profit is not None and profit < 0 else "profit"
            self._tree.insert(
                "",
                "end",
                values=(
                    position.get("symbol", "—"),
                    position.get("stockName", "—"),
                    position.get("quantity", "—"),
                    self._format_money(position.get("averagePurchasePrice")),
                    self._format_money(position.get("currentPrice")),
                    self._format_money(position.get("marketValue")),
                    self._format_signed_money(profit),
                ),
                tags=(tag,),
            )

    def _clear(self) -> None:
        for item in self._tree.get_children():
            self._tree.delete(item)

    @staticmethod
    def _decimal(value: object) -> Decimal | None:
        try:
            return Decimal(str(value))
        except (InvalidOperation, TypeError, ValueError):
            return None

    @classmethod
    def _format_money(cls, value: object) -> str:
        number = cls._decimal(value)
        return "—" if number is None else f"${number:,.2f}"

    @staticmethod
    def _format_signed_money(value: Decimal | None) -> str:
        if value is None:
            return "—"
        sign = "+" if value >= 0 else "-"
        return f"{sign}${abs(value):,.2f}"
