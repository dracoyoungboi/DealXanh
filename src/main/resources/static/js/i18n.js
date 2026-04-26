/**
 * Internationalization (i18n) Support - DealXanh
 * Multi-language support for global expansion
 */

// Supported languages
export const SUPPORTED_LANGUAGES = {
    vi: 'Tiếng Việt',
    en: 'English',
    ja: '日本語',
    ko: '한국어',
    zh: '中文',
    th: 'ไทย'
};

// Translations
const translations = {
    vi: {
        // Cart
        cart: {
            count: '0',
            add: 'Thêm',
            buyNow: 'Mua ngay'
        },
        // Deal
        deal: {
            sold: '{count} đã bán',
            slotsLeft: 'Còn {count} slot',
            freePickup: 'Miễn phí pickup',
            refund: 'Hoàn tiền 100%',
            pickupSlots: 'Khung giờ pickup',
            description: 'Mô tả sản phẩm',
            save: 'Tiết kiệm {percent}%',
            expiresSoon: '{time} nữa',
            expired: 'Đã hết hạn',
            location: 'Vị trí cửa hàng'
        },
        // Store
        store: {
            follow: 'Theo dõi',
            following: 'Đang theo dõi'
        },
        // Location
        location: {
            viewMap: 'Xem bản đồ',
            openingHours: 'Mở cửa: {hours}'
        },
        // Review
        review: {
            title: 'Đánh giá',
            viewAll: 'Xem tất cả {count}',
            loadMore: 'Xem thêm đánh giá',
            stars: '{count} sao',
            reviewCount: '({count} đánh giá)'
        },
        // Modal
        modal: {
            slotTitle: 'Chọn khung giờ pickup',
            confirm: 'Xác nhận',
            close: 'Đóng'
        },
        // Navigation
        nav: {
            home: 'Trang chủ',
            map: 'Bản đồ',
            cart: 'Giỏ hàng',
            orders: 'Đơn hàng',
            account: 'Tài khoản'
        },
        // Image
        image: {
            error: 'Không thể tải ảnh',
            loading: 'Đang tải...'
        },
        // Highlight
        highlight: {
            safe: {
                title: 'An toàn vệ sinh',
                desc: 'Được kiểm tra kỹ lưỡng'
            },
            date: {
                title: 'Date hiển thị rõ',
                desc: 'Cập nhật thường xuyên'
            },
            refund: {
                title: 'Hoàn tiền 100%',
                desc: 'Nếu không như mô tả'
            },
            source: {
                title: 'Nguồn gốc rõ ràng',
                desc: 'Cửa hàng đã được xác minh'
            }
        }
    },
    en: {
        cart: {
            count: '0',
            add: 'Add',
            buyNow: 'Buy Now'
        },
        deal: {
            sold: '{count} sold',
            slotsLeft: '{count} slots left',
            freePickup: 'Free pickup',
            refund: '100% Refund',
            pickupSlots: 'Pickup Time Slots',
            description: 'Product Description',
            save: 'Save {percent}%',
            expiresSoon: '{time} left',
            expired: 'Expired',
            location: 'Store Location'
        },
        store: {
            follow: 'Follow',
            following: 'Following'
        },
        location: {
            viewMap: 'View Map',
            openingHours: 'Open: {hours}'
        },
        review: {
            title: 'Reviews',
            viewAll: 'View All ({count})',
            loadMore: 'Load More Reviews',
            stars: '{count} stars',
            reviewCount: '({count} reviews)'
        },
        modal: {
            slotTitle: 'Select Pickup Time',
            confirm: 'Confirm',
            close: 'Close'
        },
        nav: {
            home: 'Home',
            map: 'Map',
            cart: 'Cart',
            orders: 'Orders',
            account: 'Account'
        },
        image: {
            error: 'Failed to load image',
            loading: 'Loading...'
        },
        highlight: {
            safe: {
                title: 'Safety Guaranteed',
                desc: 'Thoroughly inspected'
            },
            date: {
                title: 'Clear Expiry Date',
                desc: 'Regularly updated'
            },
            refund: {
                title: '100% Refund',
                desc: 'If not as described'
            },
            source: {
                title: 'Clear Source',
                desc: 'Verified store'
            }
        }
    },
    ja: {
        cart: {
            count: '0',
            add: '追加',
            buyNow: '今すぐ購入'
        },
        deal: {
            sold: '{count}件 sold',
            slotsLeft: '残り{count}枠',
            freePickup: '無料ピックアップ',
            refund: '100%返金',
            pickupSlots: 'ピックアップ時間',
            description: '商品説明',
            save: '{percent}%オフ',
            expiresSoon: '残り{time}',
            expired: '期限切れ',
            location: '店舗の場所'
        },
        store: {
            follow: 'フォロー',
            following: 'フォロー中'
        },
        location: {
            viewMap: '地図を見る',
            openingHours: '営業時間: {hours}'
        },
        review: {
            title: 'レビュー',
            viewAll: 'すべて見る({count})',
            loadMore: 'もっと見る',
            stars: '{count}星',
            reviewCount: '({count}件のレビュー)'
        },
        modal: {
            slotTitle: 'ピックアップ時間を選択',
            confirm: '確認',
            close: '閉じる'
        },
        nav: {
            home: 'ホーム',
            map: '地図',
            cart: 'カート',
            orders: '注文',
            account: 'アカウント'
        },
        image: {
            error: '画像を読み込めません',
            loading: '読み込み中...'
        },
        highlight: {
            safe: {
                title: '安全保証',
                desc: '厳重に検査済み'
            },
            date: {
                title: '賞味期限表示',
                desc: '定期的に更新'
            },
            refund: {
                title: '100%返金',
                desc: '説明と異なる場合'
            },
            source: {
                title: '明確な来源',
                desc: '認証された店舗'
            }
        }
    }
};

// Current language (default: Vietnamese)
let currentLanguage = 'vi';

// Detect browser language
export function detectLanguage() {
    const browserLang = navigator.language.split('-')[0];
    return SUPPORTED_LANGUAGES[browserLang] ? browserLang : 'vi';
}

// Set language
export function setLanguage(lang) {
    if (SUPPORTED_LANGUAGES[lang]) {
        currentLanguage = lang;
        localStorage.setItem('dealxanh_lang', lang);
        document.documentElement.lang = lang;
        updatePageTranslations();
    }
}

// Get current language
export function getLanguage() {
    return currentLanguage;
}

// Get translation
export function t(key, params = {}) {
    const keys = key.split('.');
    let value = translations[currentLanguage];

    for (const k of keys) {
        if (value && value[k] !== undefined) {
            value = value[k];
        } else {
            // Fallback to Vietnamese
            value = translations['vi'];
            for (const fallbackKey of keys) {
                if (value && value[fallbackKey] !== undefined) {
                    value = value[fallbackKey];
                } else {
                    return key; // Return key if translation not found
                }
            }
            break;
        }
    }

    if (typeof value === 'string') {
        // Replace placeholders
        return value.replace(/\{(\w+)\}/g, (match, paramKey) => {
            return params[paramKey] !== undefined ? params[paramKey] : match;
        });
    }

    return value;
}

// Translate element with data-i18n attribute
function translateElement(element) {
    const key = element.getAttribute('data-i18n');
    if (!key) return;

    const keys = key.split('.');
    let value = translations[currentLanguage];

    for (const k of keys) {
        if (value && value[k] !== undefined) {
            value = value[k];
        } else {
            return;
        }
    }

    if (typeof value === 'string') {
        element.textContent = value;
    }
}

// Update all translations on the page
function updatePageTranslations() {
    document.querySelectorAll('[data-i18n]').forEach(translateElement);
}

// Initialize i18n
export function initI18n() {
    // Get saved language or detect from browser
    const savedLang = localStorage.getItem('dealxanh_lang');
    currentLanguage = savedLang || detectLanguage();
    document.documentElement.lang = currentLanguage;

    // Update translations on page load
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', updatePageTranslations);
    } else {
        updatePageTranslations();
    }
}

// Export translations object for direct access
export { translations };

// Auto-initialize
initI18n();
