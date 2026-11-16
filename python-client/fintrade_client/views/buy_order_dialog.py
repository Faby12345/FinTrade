"""Modal dialog for submitting a limit buy order."""

from __future__ import annotations

from collections.abc import Callable
from decimal import Decimal, InvalidOperation
import tkinter as tk
from typing import Any

from .. import theme


class BuyOrderDialog(tk.Toplevel):
    def __init__(
        self,
        parent: tk.Widget,
        *,
        stock: dict[str, Any],
        available_cash: Decimal | None,
        on_submit: Callable[[int, Decimal], None],
    ) -> None:
        super().__init__(parent)
        self._stock = stock
        self._available_cash = available_cash
        self._on_submit = on_submit

        self.title("Place buy order")
        self.geometry("430x430")
        self.resizable(False, False)
        self.configure(background=theme.CARD_BACKGROUND)
        self.transient(parent.winfo_toplevel())
        self.grab_set()

        self.quantity = tk.StringVar(value="1")
        self.limit_price = tk.StringVar(
            value=self._initial_price(stock.get("price"))
        )
        self.status = tk.StringVar()
        self.estimated_cost = tk.StringVar()

        self.quantity.trace_add("write", self._update_estimated_cost)
        self.limit_price.trace_add("write", self._update_estimated_cost)

        self._build()
        self._update_estimated_cost()
        self.protocol("WM_DELETE_WINDOW", self.close)
        self.after_idle(self._center_over_parent)

    def _build(self) -> None:
        body = tk.Frame(
            self,
            background=theme.CARD_BACKGROUND,
            padx=28,
            pady=25,
        )
        body.pack(fill="both", expand=True)

        symbol = str(self._stock.get("symbol", "Stock"))
        company = str(self._stock.get("name", ""))
        tk.Label(
            body,
            text=f"Buy {symbol}",
            background=theme.CARD_BACKGROUND,
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 22, "bold"),
        ).pack(anchor="w")
        tk.Label(
            body,
            text=company,
            background=theme.CARD_BACKGROUND,
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 10),
        ).pack(anchor="w", pady=(4, 17))

        details = tk.Frame(body, background="#eef2ff", padx=14, pady=11)
        details.pack(fill="x", pady=(0, 17))
        tk.Label(
            details,
            text=f"Available: {self._money(self._available_cash)}",
            background="#eef2ff",
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 10, "bold"),
        ).pack(side="left")
        tk.Label(
            details,
            text=f"Market: {self._money(self._stock.get('price'))}",
            background="#eef2ff",
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 10),
        ).pack(side="right")

        self._entry(body, "Quantity", self.quantity)
        self._entry(body, "Limit price", self.limit_price)

        tk.Label(
            body,
            textvariable=self.estimated_cost,
            background=theme.CARD_BACKGROUND,
            foreground=theme.MUTED,
            font=(theme.FONT_FAMILY, 9, "bold"),
        ).pack(anchor="w")
        tk.Label(
            body,
            textvariable=self.status,
            background=theme.CARD_BACKGROUND,
            foreground=theme.ERROR,
            wraplength=360,
            justify="left",
            font=(theme.FONT_FAMILY, 9),
        ).pack(anchor="w", pady=(6, 5))

        actions = tk.Frame(body, background=theme.CARD_BACKGROUND)
        actions.pack(fill="x", side="bottom")
        tk.Button(
            actions,
            text="Cancel",
            command=self.close,
            relief="flat",
            background="#e2e8f0",
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 10, "bold"),
            padx=18,
            pady=9,
            cursor="hand2",
        ).pack(side="right")
        self._submit_button = tk.Button(
            actions,
            text="Place buy order",
            command=self._submit,
            relief="flat",
            background=theme.SUCCESS,
            activebackground="#15803d",
            foreground="white",
            activeforeground="white",
            font=(theme.FONT_FAMILY, 10, "bold"),
            padx=18,
            pady=9,
            cursor="hand2",
        )
        self._submit_button.pack(side="right", padx=(0, 9))

    def _entry(
        self,
        parent: tk.Widget,
        label: str,
        variable: tk.StringVar,
    ) -> None:
        tk.Label(
            parent,
            text=label,
            background=theme.CARD_BACKGROUND,
            foreground=theme.TEXT,
            font=(theme.FONT_FAMILY, 9, "bold"),
        ).pack(anchor="w", pady=(0, 5))
        tk.Entry(
            parent,
            textvariable=variable,
            relief="flat",
            highlightthickness=1,
            highlightbackground=theme.BORDER,
            highlightcolor=theme.PRIMARY,
            background=theme.FIELD_BACKGROUND,
            foreground=theme.TEXT,
            insertbackground=theme.TEXT,
            font=(theme.FONT_FAMILY, 11),
        ).pack(fill="x", ipady=8, pady=(0, 12))

    def _submit(self) -> None:
        try:
            quantity = int(self.quantity.get().strip())
        except ValueError:
            self.show_error("Quantity must be a whole number.")
            return

        if quantity <= 0:
            self.show_error("Quantity must be greater than zero.")
            return

        try:
            price = Decimal(self.limit_price.get().strip())
        except InvalidOperation:
            self.show_error("Limit price must be a valid number.")
            return

        if not price.is_finite() or price <= 0:
            self.show_error("Limit price must be greater than zero.")
            return

        maximum_cost = price * quantity
        if (
            self._available_cash is not None
            and maximum_cost > self._available_cash
        ):
            self.show_error(
                f"This order needs {self._money(maximum_cost)}, "
                f"but only {self._money(self._available_cash)} is available."
            )
            return

        self.status.set("Submitting order…")
        self._submit_button.configure(state="disabled", cursor="arrow")
        self._on_submit(quantity, price)

    def _update_estimated_cost(self, *_args: object) -> None:
        try:
            quantity = int(self.quantity.get().strip())
            price = Decimal(self.limit_price.get().strip())
            if quantity <= 0 or not price.is_finite() or price <= 0:
                raise ValueError
            self.estimated_cost.set(
                f"Maximum reserved cash: {self._money(price * quantity)}"
            )
        except (InvalidOperation, ValueError):
            self.estimated_cost.set("Maximum reserved cash: —")

    def show_error(self, message: str) -> None:
        self.status.set(message)
        self._submit_button.configure(state="normal", cursor="hand2")

    def close(self) -> None:
        self.grab_release()
        self.destroy()

    def _center_over_parent(self) -> None:
        self.update_idletasks()
        parent = self.master.winfo_toplevel()
        x = parent.winfo_rootx() + (parent.winfo_width() - self.winfo_width()) // 2
        y = parent.winfo_rooty() + (parent.winfo_height() - self.winfo_height()) // 2
        self.geometry(f"+{max(x, 0)}+{max(y, 0)}")

    @staticmethod
    def _initial_price(value: object) -> str:
        try:
            return format(Decimal(str(value)), ".2f")
        except (InvalidOperation, TypeError, ValueError):
            return ""

    @staticmethod
    def _money(value: object) -> str:
        try:
            return f"${Decimal(str(value)):,.2f}"
        except (InvalidOperation, TypeError, ValueError):
            return "—"
