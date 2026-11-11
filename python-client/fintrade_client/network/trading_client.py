"""Synchronous, newline-delimited JSON client for the trading server."""

from __future__ import annotations

import json
import socket
import threading
import uuid
from typing import Any, TextIO


class TradingClient:
    """Maintains one TCP connection and its server-side client session."""

    def __init__(
        self,
        host: str = "127.0.0.1",
        port: int = 9090,
        timeout_seconds: float = 5.0,
    ) -> None:
        self._host = host
        self._port = port
        self._timeout_seconds = timeout_seconds

        self._socket: socket.socket | None = None
        self._reader: TextIO | None = None
        self._writer: TextIO | None = None
        self._request_lock = threading.RLock()

    def request(
        self,
        request_type: str,
        payload: dict[str, Any],
    ) -> dict[str, Any]:
        """Send one request and wait for its matching response."""

        with self._request_lock:
            self._connect_if_needed()

            reader = self._reader
            writer = self._writer

            if reader is None or writer is None:
                raise ConnectionError("The client is not connected")

            request_id = str(uuid.uuid4())
            request = {
                "requestId": request_id,
                "type": request_type,
                "payload": payload,
            }

            try:
                writer.write(json.dumps(request) + "\n")
                writer.flush()

                response_line = reader.readline()

                if not response_line:
                    raise ConnectionError(
                        "The server closed the connection"
                    )

                response = json.loads(response_line)

                if not isinstance(response, dict):
                    raise ConnectionError(
                        "The server returned an invalid response"
                    )

                if response.get("requestId") != request_id:
                    raise ConnectionError(
                        "The server returned an unexpected response"
                    )

                return response
            except (OSError, ValueError, ConnectionError):
                self.close()
                raise

    def close(self) -> None:
        """Close the connection and discard its authenticated session."""

        with self._request_lock:
            reader = self._reader
            writer = self._writer
            client_socket = self._socket

            self._reader = None
            self._writer = None
            self._socket = None

            for resource in (reader, writer, client_socket):
                if resource is not None:
                    try:
                        resource.close()
                    except OSError:
                        pass

    def _connect_if_needed(self) -> None:
        if self._socket is not None:
            return

        client_socket = socket.create_connection(
            (self._host, self._port),
            timeout=self._timeout_seconds,
        )

        self._socket = client_socket
        self._reader = client_socket.makefile(
            "r",
            encoding="utf-8",
            newline="\n",
        )
        self._writer = client_socket.makefile(
            "w",
            encoding="utf-8",
            newline="\n",
        )
