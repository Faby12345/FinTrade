"""Position operations exposed to the GUI."""

from __future__ import annotations

from typing import Any

from ..network.trading_client import TradingClient


class PositionService:
    def __init__(self, trading_client: TradingClient) -> None:
        self._trading_client = trading_client

    def list_my_positions(self) -> dict[str, Any]:
        return self._trading_client.request("GET_MY_POSITIONS", {})
