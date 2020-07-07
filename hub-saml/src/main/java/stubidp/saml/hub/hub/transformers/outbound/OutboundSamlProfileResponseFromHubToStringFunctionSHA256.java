package stubidp.saml.hub.hub.transformers.outbound;

import stubidp.saml.domain.response.OutboundResponseFromHub;

import java.util.function.Function;

public class OutboundSamlProfileResponseFromHubToStringFunctionSHA256 implements Function<OutboundResponseFromHub, String> {
    private Function<OutboundResponseFromHub, String> transformer;

    public OutboundSamlProfileResponseFromHubToStringFunctionSHA256(Function<OutboundResponseFromHub,String> transformer) {
        this.transformer = transformer;
    }

    @Override
    public String apply(OutboundResponseFromHub outboundResponseFromHub) {
        return transformer.apply(outboundResponseFromHub);
    }
}
