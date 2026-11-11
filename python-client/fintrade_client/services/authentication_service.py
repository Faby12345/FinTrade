"""Authentication operations exposed to the GUI."""

from __future__ import annotations

from typing import Any

from ..network.trading_client import TradingClient


class AuthenticationService:
    def __init__(self, trading_client: TradingClient) -> None:
        self._trading_client = trading_client

    def login(
        self,
        email: str,
        password: str,
    ) -> dict[str, Any]:
        return self._trading_client.request(
            "LOGIN",
            {
                "email": email,
                "password": password,
            },
        )

    def register(
        self,
        username: str,
        email: str,
        password: str,
    ) -> dict[str, Any]:
        return self._trading_client.request(
            "REGISTER",
            {
                "username": username,
                "email": email,
                "password": password,
            },
        )
