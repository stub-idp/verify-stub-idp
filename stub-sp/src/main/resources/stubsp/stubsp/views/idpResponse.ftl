
<div class="responseStatus">
<b>Response Status:</b> ${samlResponseFromIdpDto.responseStatus}
</div>

<div class="relayState">
<b>Relay State:</b> ${samlResponseFromIdpDto.relayState}
</div>

<h2>Parsed Response:</h2>
<pre><code><div class="dto" style="color: white; background-color: black;">
${samlResponseFromIdpDto.jsonResponse}
</div></code></pre>

<h2>Saml Response:</h2>
<pre><code><div class="saml" style="color: white; background-color: black;">
${samlResponseFromIdpDto.xmlResponse}
</div></code></pre>
