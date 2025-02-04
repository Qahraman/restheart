/*
 * RESTHeart - the data REST API server
 * Copyright (C) 2014 - 2015 SoftInstigate Srl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.restheart.security.handlers;

import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import org.restheart.handlers.PipedHttpHandler;
import org.restheart.handlers.RequestContext;
import org.restheart.security.AccessManager;

/**
 *
 * @author Andrea Di Cesare <andrea@softinstigate.com>
 */
public class AuthenticationConstraintHandler extends PipedHttpHandler {

    AccessManager am;

    /**
     *
     * @param next
     * @param am
     */
    public AuthenticationConstraintHandler(PipedHttpHandler next, AccessManager am) {
        super(next);
        this.am = am;
    }
    
    protected boolean isAuthenticationRequired(final HttpServerExchange exchange) {
        return am.isAuthenticationRequired(exchange);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange, RequestContext context) throws Exception {
        if (isAuthenticationRequired(exchange)) {
            SecurityContext scontext = exchange.getSecurityContext();
            scontext.setAuthenticationRequired();
        }

        getNext().handleRequest(exchange, context);
    }
}