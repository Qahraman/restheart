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
package org.restheart.handlers.document;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.bson.types.ObjectId;
import org.restheart.db.DocumentDAO;
import org.restheart.db.OperationResult;
import org.restheart.handlers.PipedHttpHandler;
import org.restheart.handlers.RequestContext;
import org.restheart.utils.HttpStatus;
import org.restheart.utils.RequestHelper;
import org.restheart.utils.ResponseHelper;

/**
 *
 * @author Andrea Di Cesare <andrea@softinstigate.com>
 */
public class DeleteDocumentHandler extends PipedHttpHandler {

    private final DocumentDAO documentDAO;

    /**
     * Default ctor
     */
    public DeleteDocumentHandler() {
        this(new DocumentDAO());
    }

    /**
     * Creates a new instance of DeleteDocumentHandler
     *
     * @param documentDAO
     */
    public DeleteDocumentHandler(DocumentDAO documentDAO) {
        super(null);
        this.documentDAO = documentDAO;
    }

    /**
     *
     * @param exchange
     * @param context
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange exchange, RequestContext context) throws Exception {
        ObjectId etag = RequestHelper.getWriteEtag(exchange);

        if (etag == null) {
            ResponseHelper.endExchangeWithMessage(exchange, HttpStatus.SC_CONFLICT,
                    "The document's ETag must be provided using the '" + Headers.IF_MATCH + "' header");
            return;
        }

        OperationResult result = this.documentDAO
                .deleteDocument(context.getDBName(), context.getCollectionName(), context.getDocumentId(), etag);

        if (result.getEtag() != null) {
            exchange.getResponseHeaders().put(Headers.ETAG, result.getEtag().toString());
        }

        // send the warnings if any (and in case no_content change the return code to ok
        if (context.getWarnings() != null && !context.getWarnings().isEmpty()) {
            sendWarnings(result.getHttpCode(), exchange, context);
        } else {
            exchange.setResponseCode(result.getHttpCode());
        }

        exchange.endExchange();
    }
}
