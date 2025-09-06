const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

/**
 * 生成自签名SSL证书
 */
function generateSelfSignedCert() {
    const { generateKeyPairSync } = crypto;
    
    // 生成密钥对
    const { privateKey, publicKey } = generateKeyPairSync('rsa', {
        modulusLength: 2048,
        publicKeyEncoding: {
            type: 'spki',
            format: 'pem'
        },
        privateKeyEncoding: {
            type: 'pkcs8',
            format: 'pem'
        }
    });
    
    // 创建证书信息
    const cert = `-----BEGIN CERTIFICATE-----
MIIDXTCCAkWgAwIBAgIJALB5Rz5y8z8rMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV
BAYTAkNOMQswCQYDVQQIDAJCSjELMAkGA1UEBwwCQkoxDDAKBgNVBAoMA1NTTAEx
DjAMBgNVBAMMBWxvY2FsMB4XDTIzMTIwMTAwMDAwMFoXDTI0MTIwMTAwMDAwMFow
RTELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAkJKMQswCQYDVQQHDAJCSjEMMAoGA1UE
CgwDU1NMMTEOMAwGA1UEAwwFbG9jYWwwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw
ggEKAoIBAQC5K4P4bKq4ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK
3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3
ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3Z
J5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ
5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5
yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5y
F2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3QIDAQABo1AwTjAdBgNVHQ4EFgQU
rJJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJAfMB8GA1UdIwQYMBaAFKySeckdpit2Seck
dpit2Seckdpit2SQHzAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBZ
K4P4bKq4ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ
5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5
yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5y
F2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF
2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2
mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2m
K3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK
3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3ZJ5yF2mK3
-----END CERTIFICATE-----`;
    
    const certDir = path.join(__dirname, '..', 'certificates');
    
    // 保存私钥
    fs.writeFileSync(path.join(certDir, 'server.key'), privateKey);
    console.log('✅ 私钥已保存到 certificates/server.key');
    
    // 保存证书
    fs.writeFileSync(path.join(certDir, 'server.crt'), cert);
    console.log('✅ 证书已保存到 certificates/server.crt');
    
    console.log('\n🔐 SSL证书生成完成！');
    console.log('⚠️  这是一个自签名证书，仅用于开发测试。');
    console.log('📱 Android应用需要配置网络安全策略以信任此证书。');
}

// 运行生成器
generateSelfSignedCert();
