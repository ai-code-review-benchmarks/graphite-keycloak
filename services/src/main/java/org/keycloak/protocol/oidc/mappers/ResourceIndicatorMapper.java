/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.protocol.oidc.mappers;

import org.keycloak.OAuth2Constants;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adds the allowed requested resources to the {@code aud} claim of an access token.
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class ResourceIndicatorMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, TokenIntrospectionTokenMapper {

    public static final String PROVIDER_ID = "oidc-resource-indicator-mapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        List<ProviderConfigProperty> props = ProviderConfigurationBuilder.create()
//                .property()
//                .name(RESOURCES_PROPERTY)
//                .type(ProviderConfigProperty.MULTIVALUED_STRING_TYPE)
//                .label(RESOURCES_LABEL)
//                .helpText(RESOURCES_HELP_TEXT)
//                .add()
                .build();

        configProperties.addAll(props);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, ResourceIndicatorMapper.class);
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Resource Indicators";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Adds requested OAuth2 Resource Indicators to audience claim.";
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {

        Set<?> resourceIndicatorsToAdd = clientSessionCtx.getAttribute(OAuth2Constants.RESOURCE, Set.class);
        if (resourceIndicatorsToAdd == null || resourceIndicatorsToAdd.isEmpty()) {
            return;
        }

        // remove all resource indicators that are already contained in the audience.
        String[] audience = token.getAudience();
        if (audience != null && audience.length > 0) {
            resourceIndicatorsToAdd.removeAll(Set.of(audience));
        }

        for (Object resourceIndicator : resourceIndicatorsToAdd) {
            token.addAudience(String.valueOf(resourceIndicator));
        }
    }

    public static ProtocolMapperModel create(String name, boolean accessToken, boolean introspectionEndpoint) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        if (accessToken) {
            config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        }
        if (introspectionEndpoint) {
            config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        }
        mapper.setConfig(config);
        return mapper;
    }

}
