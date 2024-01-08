package com.jagrosh.giveawaybot.entities;

import com.jagrosh.interactions.requests.Route;

public class PinRoute extends Route.FormattedRoute
{
    public PinRoute(long channelId, long messageId, Route.Type type)
    {
        super(type, Route.BASE_URL + String.format("channels/%d/pins/%d", channelId, messageId));
    }
}
