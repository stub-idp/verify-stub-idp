
<div class="responseStatus">
<b>Response Status:</b> ${samlResponseFromIdpDto.responseStatus}
</div>

<div class="relayState">
<b>Relay State:</b> ${samlResponseFromIdpDto.relayState}
</div>

<b>Parsed Response:</b>
<div class="dto">
${samlResponseFromIdpDto.jsonResponse}
</div>

<b>Saml Response:</b>
<div class="saml">
${samlResponseFromIdpDto.xmlResponse}
</div>
