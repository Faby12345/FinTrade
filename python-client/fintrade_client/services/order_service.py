"""Order operations exposed to the GUI."""

from __future__ import annotations

from decimal import Decimal
from typing import Any

from ..network.trading_client import TradingClient


class OrderService:
    def __init__(self, trading_client: TradingClient) -> None:
        self._trading_client = trading_client

    def list_my_orders(self) -> dict[str, Any]:
        return self._trading_client.request("LIST_MY_ORDERS", {})

    def place_sell_order(
        self,
        stock_id: str,
        quantity: int,
        limit_price: Decimal,
    ) -> dict[str, Any]:
        return self._trading_client.request(
            "PLACE_ORDER",
            {
                "stockId": stock_id,
                "side": "SELL",
                "quantity": quantity,
                "limitPrice": float(limit_price),
            },
        )
