<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
    ${rb.loginTotpTitle}

    <#elseif section = "header">
    ${rb.loginTotpTitle}

    <#elseif section = "form">
    <ol id="kc-totp-settings">
        <li>
            <p>${rb.loginTotpStep1_1} <a href="http://code.google.com/p/google-authenticator/" target="_blank">${rb.loginTotpStep1_2}</a> ${rb.loginTotpStep1_3}</p>
        </li>
        <li>
            <p>${rb.loginTotpStep2}</p>
            <img src="${totp.totpSecretQrCodeUrl}" alt="Figure: Barcode">
            <span class="code">${totp.totpSecretEncoded}</span>
        </li>
        <li>
            <p>${rb.loginTotpStep3}</p>
        </li>
    </ol>
    <form action="${url.loginUpdateTotpUrl}" id="kc-totp-settings-form" method="post">
        <div class="field-wrapper">
            <label for="otp" class="two-lines">${rb.loginTotpOneTime}</label><input type="text" id="totp" name="totp" />
            <input type="hidden" id="totpSecret" name="totpSecret" value="${totp.totpSecret}" />
        </div>
        <input type="submit" class="btn-primary" value="${rb.submit}" />
    </form>
    </#if>
</@layout.registrationLayout>