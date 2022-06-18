# paillier-lib

paillier同态加密算法库，具体包括：

## 关键特性

- 公私钥对生成和编解码
- 密文的paillier加法同态运算
- java库提供了完整的同态功能支持

## Java库说明

### 公私钥模块（PaillierKeyPair）

- 接口名称：generateGoodKeyPair
- 接口功能说明：生成同态加密的公私钥对，2048位

| 输入     | 类型     | 说明                                   |
| :----------- | :----------- | :----------------------------------------- |
| 无    |           |             |
| **输出** | **类型** | **说明**                               |
| 返回值       | KeyPair      | 生成的密钥对  |

- 接口名称：generateStrongKeyPair
- 接口功能说明：生成同态加密的公私钥对，4096位

| 输入     | 类型     | 说明                                   |
| :----------- | :----------- | :----------------------------------------- |
| 无    |           |             |
| **输出** | **类型** | **说明**                               |
| 返回值       | KeyPair      | 生成的密钥对  |


### 同态算法模块（PaillierCipher）

- 接口名称：encrypt
- 接口功能说明：对数据进行同态加密

| 输入     | 类型     | 说明                         |
| :----------- | :----------- | :------------------------------- |
| m            | BigInteger   | 待加密的操作数                   |
| publickey    | PublicKey    | 加密公钥，可以通过公私钥模块获取 |
| **输出** | **类型** | **说明**                     |
| 返回值       | String       | 成功：密文，    失败：空串         |

- 接口名称：decrypt
- 接口功能说明：对加密数据进行解密还原操作数

| 输入     | 类型     | 说明                         |
| :----------- | :----------- | :------------------------------- |
| ciphertext   | String       | 密文                             |
| privateKey   | PrivateKey   | 解密私钥，可以通过公私钥模块获取 |
| **输出** | **类型** | **说明**                     |
| 返回值       | BigInteger   | 成功：明文，    失败：空串         |

- 接口名称：ciphertextAdd
- 接口功能说明：加法同态实现

| 输入     | 类型     | 说明                           |
| :----------- | :----------- | :--------------------------------- |
| ciphertext1  | String       | 同态加密后的操作数1                |
| ciphertext2  | String       | 同态加密后的操作数2                |
| **输出** | **类型** | **说明**                       |
| 返回值       | String       | 成功：密文，    失败：空串 |

### 使用教程


**开发示例**

```java
// generate the key pair for encrypt and decrypt
KeyPair keypair = PaillierKeyPair.generateGoodKeyPair();
RSAPublicKey pubKey = (RSAPublicKey)keypair.getPublic();
RSAPrivateKey priKey = (RSAPrivateKey)keypair.getPrivate();

// encrypt the first number with public key
BigInteger i1 = BigInteger.valueOf(1000000);
String c1 = PaillierCipher.encrypt(i1, pubKey);

// encrypt the second number with same public key
BigInteger i2 = BigInteger.valueOf(2012012012);
String c2 = PaillierCipher.encrypt(i2, pubKey);

// paillier add with numbers
String c3 = PaillierCipher.ciphertextAdd(c1,c2);

// decrypt the result
BigInteger o3 = PaillierCipher.decrypt(c3, priKey);
```

