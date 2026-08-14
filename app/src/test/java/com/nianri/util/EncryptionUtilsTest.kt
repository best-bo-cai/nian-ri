package com.nianri.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EncryptionUtilsTest {

    @Test
    fun `encrypt 对普通文本返回加密后的 Base64 字符串`() {
        val plainText = "sk-test-123456"
        val encrypted = EncryptionUtils.encrypt(plainText)
        assertTrue(encrypted.isNotEmpty())
        assertNotEquals(plainText, encrypted)
    }

    @Test
    fun `decrypt 能还原 encrypt 的结果`() {
        val plainText = "my-secret-api-key"
        val encrypted = EncryptionUtils.encrypt(plainText)
        assertEquals(plainText, EncryptionUtils.decrypt(encrypted))
    }

    @Test
    fun `encrypt 空字符串返回空字符串`() {
        assertEquals("", EncryptionUtils.encrypt(""))
    }

    @Test
    fun `decrypt 空字符串返回空字符串`() {
        assertEquals("", EncryptionUtils.decrypt(""))
    }

    @Test
    fun `encrypt 与 decrypt 往返测试含中文与特殊字符`() {
        val plainText = "密钥@#$%^&* 中文内容 123"
        val encrypted = EncryptionUtils.encrypt(plainText)
        assertEquals(plainText, EncryptionUtils.decrypt(encrypted))
    }

    @Test
    fun `decrypt 非法密文回退为原文`() {
        val invalid = "not-a-valid-base64!!!"
        assertEquals(invalid, EncryptionUtils.decrypt(invalid))
    }

    @Test
    fun `encrypt 相同明文每次加密结果一致（ECB 模式确定性）`() {
        val plainText = "deterministic-test"
        assertEquals(EncryptionUtils.encrypt(plainText), EncryptionUtils.encrypt(plainText))
    }
}
