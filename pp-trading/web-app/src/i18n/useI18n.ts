import { useState, useCallback } from 'react'
import zh, { type TranslationKey } from './zh'
import en from './en'

/** 支持的语言类型 */
export type Lang = 'zh' | 'en'

/** 语言包映射 */
const bundles: Record<Lang, Record<TranslationKey, string>> = { zh, en }

/** localStorage 存储 key */
const LANG_STORAGE_KEY = 'pp-lang'

/**
 * 从 localStorage 读取语言偏好，默认中文
 */
function loadLang(): Lang {
  const saved = localStorage.getItem(LANG_STORAGE_KEY)
  if (saved === 'en') return 'en'
  return 'zh'
}

/**
 * 国际化 Hook。
 * 提供 t 函数翻译文案、lang 当前语言、toggleLang 切换语言。
 *
 * 用法: t('search.symbol')  →  "股票代码" / "Symbol"
 * 带参数: t('toolbar.totalData', { count: 100 })  →  "共 100 条数据" / "100 records"
 */
export function useI18n() {
  const [lang, setLang] = useState<Lang>(loadLang)

  /** 切换中英文 */
  const toggleLang = useCallback(() => {
    setLang(prev => {
      const next = prev === 'zh' ? 'en' : 'zh'
      localStorage.setItem(LANG_STORAGE_KEY, next)
      return next
    })
  }, [])

  /** 翻译函数，支持 {key} 占位符替换 */
  const t = useCallback(
    (key: TranslationKey, params?: Record<string, string | number>): string => {
      let text = bundles[lang][key] ?? bundles.zh[key] ?? key
      if (params) {
        for (const [k, v] of Object.entries(params)) {
          text = text.replace(`{${k}}`, String(v))
        }
      }
      return text
    },
    [lang],
  )

  return { t, lang, toggleLang }
}
