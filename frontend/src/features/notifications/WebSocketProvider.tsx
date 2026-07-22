import { useEffect } from 'react';
import { Client, type IMessage } from '@stomp/stompjs';
import { useQueryClient } from '@tanstack/react-query';
import { getAccessToken } from '../../api/client';
import { useAuth } from '../../auth/useAuth';

// invalida as queries de notificações sempre que chega uma mensagem pra este utilizador
function onNotification(_frame: IMessage, queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: ['notifications'] });
}

function WebSocketProvider() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!user) {
      return;
    }

    const client = new Client({
      brokerURL: `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws`,
      reconnectDelay: 5000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,
      // o access token pode ser renovado entretanto; lê-se fresco antes de cada ligação
      beforeConnect: () => {
        const token = getAccessToken();
        client.connectHeaders = token ? { Authorization: `Bearer ${token}` } : {};
      },
      onConnect: () => {
        client.subscribe('/user/queue/notifications', (frame) => onNotification(frame, queryClient));
      },
      // falhas de ligação são normais entre reconexões; não vale a pena logar
      onStompError: () => undefined,
    });

    client.activate();

    return () => {
      void client.deactivate();
    };
  }, [user, queryClient]);

  return null;
}

export default WebSocketProvider;