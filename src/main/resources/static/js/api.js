/**
 * API Client - DealXanh
 * Mock API for development purposes
 */

// Base URL (update with actual API endpoint when ready)
const BASE_URL = '/api';

// Mock data for development
const mockData = {
    deals: [
        {
            id: '1',
            title: 'Bánh mì pate nóng hổi - Giảm 50%',
            storeId: 'store1',
            storeName: 'Bánh mì Hòa Mai',
            storeAvatar: 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=80&q=80',
            originalPrice: 25000,
            price: 12500,
            discount: 50,
            expiryDate: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(), // 2 hours from now
            pickupDeadline: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400&q=80',
            category: 'food',
            distance: 350,
            location: { lat: 21.0285, lng: 105.8542 },
            pickupSlots: [
                { id: 1, time: '17:00 - 18:00', available: 5 },
                { id: 2, time: '18:00 - 19:00', available: 3 },
                { id: 3, time: '19:00 - 20:00', available: 8 }
            ],
            description: 'Bánh mì pate đặc biệt, nhân thịt nguội, đồ chua, rau sống. Bánh mới ra lò nóng hổi, giòn rụm.',
            rating: 4.5,
            reviewCount: 128
        },
        {
            id: '2',
            title: 'Combo bún bò Huế cận date',
            storeId: 'store2',
            storeName: 'Bún bò Thảo',
            storeAvatar: 'https://images.unsplash.com/photo-1555126634-323283e090fa?w=80&q=80',
            originalPrice: 45000,
            price: 27000,
            discount: 40,
            expiryDate: new Date(Date.now() + 1 * 60 * 60 * 1000).toISOString(), // 1 hour from now
            pickupDeadline: new Date(Date.now() + 1 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=400&q=80',
            category: 'food',
            distance: 520,
            location: { lat: 21.0325, lng: 105.8512 },
            pickupSlots: [
                { id: 1, time: '17:30 - 18:30', available: 2 },
                { id: 2, time: '18:30 - 19:30', available: 6 }
            ],
            description: 'Bún bò Huế chuẩn vị, nước dùng đậm đà, thịt bò tươi, chả cua thơm ngon.',
            rating: 4.7,
            reviewCount: 89
        },
        {
            id: '3',
            title: 'Blind Box Mystery - 3 món bất ngờ',
            storeId: 'store3',
            storeName: 'Cơm Niêu Nhà',
            storeAvatar: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=80&q=80',
            originalPrice: 80000,
            price: 48000,
            discount: 40,
            expiryDate: new Date(Date.now() + 3 * 60 * 60 * 1000).toISOString(),
            pickupDeadline: new Date(Date.now() + 3 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80',
            category: 'blindbox',
            distance: 780,
            location: { lat: 21.0255, lng: 105.8582 },
            pickupSlots: [
                { id: 1, time: '18:00 - 19:00', available: 10 },
                { id: 2, time: '19:00 - 20:00', available: 15 }
            ],
            description: 'Blind Box đặc biệt! Chứa 3 món ăn bí ẩn với giá trị ít nhất 80.000đ. Giải mã ngay!',
            rating: 4.3,
            reviewCount: 45,
            isBlindBox: true
        },
        {
            id: '4',
            title: 'Trà sữa trân châu đường đen cận date',
            storeId: 'store4',
            storeName: 'Mixue Hòa Lạc',
            storeAvatar: 'https://images.unsplash.com/photo-1579954115545-a95591f28bfc?w=80&q=80',
            originalPrice: 30000,
            price: 15000,
            discount: 50,
            expiryDate: new Date(Date.now() + 30 * 60 * 1000).toISOString(), // 30 minutes
            pickupDeadline: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&q=80',
            category: 'beverage',
            distance: 1200,
            location: { lat: 21.0305, lng: 105.8502 },
            pickupSlots: [
                { id: 1, time: 'Ngay', available: 20 }
            ],
            description: 'Trà sữa trân châu đường đen thơm ngon, làm từ sữa tươi nguyên chất.',
            rating: 4.2,
            reviewCount: 156
        },
        {
            id: '5',
            title: 'Gà rán KFC cận date - 2 miếng',
            storeId: 'store5',
            storeName: 'KFC Hòa Lạc',
            storeAvatar: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=80&q=80',
            originalPrice: 55000,
            price: 33000,
            discount: 40,
            expiryDate: new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString(),
            pickupDeadline: new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=400&q=80',
            category: 'food',
            distance: 1500,
            location: { lat: 21.0225, lng: 105.8562 },
            pickupSlots: [
                { id: 1, time: '17:00 - 18:00', available: 4 },
                { id: 2, time: '18:00 - 19:00', available: 7 }
            ],
            description: 'Gà rán giòn tan, tẩm vị đặc biệt KFC. 2 miếng gà + 1 khoai tây.',
            rating: 4.6,
            reviewCount: 234
        },
        {
            id: '6',
            title: 'Blind Box Sweet - 3 món ngọt ngào',
            storeId: 'store6',
            storeName: 'Bánh Mousse',
            storeAvatar: 'https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=80&q=80',
            originalPrice: 60000,
            price: 36000,
            discount: 40,
            expiryDate: new Date(Date.now() + 5 * 60 * 60 * 1000).toISOString(),
            pickupDeadline: new Date(Date.now() + 5 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=400&q=80',
            category: 'blindbox',
            distance: 950,
            location: { lat: 21.0280, lng: 105.8522 },
            pickupSlots: [
                { id: 1, time: '19:00 - 20:00', available: 8 }
            ],
            description: 'Blind Box đồ ngọt! 3 món bánh bí ẩn từ tiệm Bánh Mousse nổi tiếng.',
            rating: 4.4,
            reviewCount: 67,
            isBlindBox: true
        }
    ],

    stores: [
        {
            id: 'store1',
            name: 'Bánh mì Hòa Mai',
            address: '123 Đường Thống Nhất, Hòa Lạc',
            phone: '0123456789',
            rating: 4.5,
            reviewCount: 128,
            avatar: 'https://via.placeholder.com/80',
            verified: true
        },
        {
            id: 'store2',
            name: 'Bún bò Thảo',
            address: '456 Đường Nguyễn Trãi, Hòa Lạc',
            phone: '0123456790',
            rating: 4.7,
            reviewCount: 89,
            avatar: 'https://via.placeholder.com/80',
            verified: true
        }
    ],

    user: {
        id: 'user1',
        name: 'Nguyễn Văn A',
        email: 'nguyenvan@email.com',
        phone: '0123456789',
        avatar: 'https://via.placeholder.com/80',
        balance: 150000
    }
};

// API Client class
class ApiClient {
    constructor() {
        this.baseUrl = BASE_URL;
        this.useMock = true; // Set to false when using real API
    }

    // Generic request method
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;

        if (this.useMock) {
            return this.mockRequest(endpoint, options);
        }

        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return response.json();
    }

    // Mock request handler
    async mockRequest(endpoint, options) {
        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 300));

        const { method = 'GET' } = options;

        switch (true) {
            case endpoint === '/deals' && method === 'GET':
                return { success: true, data: mockData.deals };

            case endpoint.match(/^\/deals\/\w+$/) && method === 'GET':
                const dealId = endpoint.split('/')[2];
                const deal = mockData.deals.find(d => d.id === dealId);
                return deal ? { success: true, data: deal } : { success: false, error: 'Deal not found' };

            case endpoint === '/auth/login' && method === 'POST':
                return { success: true, data: { ...mockData.user, token: 'mock-token-123' } };

            case endpoint === '/auth/register' && method === 'POST':
                return { success: true, data: { ...mockData.user, token: 'mock-token-123' } };

            case endpoint === '/auth/verify-otp' && method === 'POST':
                return { success: true, data: { ...mockData.user, token: 'mock-token-123' } };

            case endpoint === '/user/profile' && method === 'GET':
                return { success: true, data: mockData.user };

            case endpoint === '/stores' && method === 'GET':
                return { success: true, data: mockData.stores };

            default:
                return { success: false, error: 'Endpoint not found' };
        }
    }

    // GET request
    get(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'GET' });
    }

    // POST request
    post(endpoint, data, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    // PUT request
    put(endpoint, data, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    // DELETE request
    delete(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'DELETE' });
    }
}

// Create API client instance
const api = new ApiClient();

// API methods
export const authApi = {
    async login(email, password) {
        return api.post('/auth/login', { email, password });
    },

    async register(data) {
        return api.post('/auth/register', data);
    },

    async verifyOtp(phone, otp) {
        return api.post('/auth/verify-otp', { phone, otp });
    },

    async sendOtp(phone) {
        return api.post('/auth/send-otp', { phone });
    },

    async logout() {
        return api.post('/auth/logout');
    },

    async forgotPassword(email) {
        return api.post('/auth/forgot-password', { email });
    },

    async resetPassword(token, newPassword) {
        return api.post('/auth/reset-password', { token, newPassword });
    }
};

export const dealsApi = {
    async getAll(filters = {}) {
        const queryParams = new URLSearchParams(filters).toString();
        return api.get(`/deals?${queryParams}`);
    },

    async getById(id) {
        return api.get(`/deals/${id}`);
    },

    async search(keyword, filters = {}) {
        return api.get(`/deals/search?q=${keyword}&${new URLSearchParams(filters).toString()}`);
    },

    async getByCategory(category, filters = {}) {
        return api.get(`/deals/category/${category}?${new URLSearchParams(filters).toString()}`);
    },

    async getByStore(storeId, filters = {}) {
        return api.get(`/deals/store/${storeId}?${new URLSearchParams(filters).toString()}`);
    },

    async getNearMe(lat, lng, radius = 3000, filters = {}) {
        return api.get(`/deals/near?lat=${lat}&lng=${lng}&radius=${radius}&${new URLSearchParams(filters).toString()}`);
    }
};

export const storesApi = {
    async getAll(filters = {}) {
        return api.get(`/stores?${new URLSearchParams(filters).toString()}`);
    },

    async getById(id) {
        return api.get(`/stores/${id}`);
    }
};

export const ordersApi = {
    async create(data) {
        return api.post('/orders', data);
    },

    async getById(id) {
        return api.get(`/orders/${id}`);
    },

    async getAll(filters = {}) {
        return api.get(`/orders?${new URLSearchParams(filters).toString()}`);
    },

    async cancel(id, reason) {
        return api.post(`/orders/${id}/cancel`, { reason });
    }
};

export const cartApi = {
    async get() {
        return api.get('/cart');
    },

    async addItem(data) {
        return api.post('/cart/items', data);
    },

    async updateItem(id, data) {
        return api.put(`/cart/items/${id}`, data);
    },

    async removeItem(id) {
        return api.delete(`/cart/items/${id}`);
    },

    async clear() {
        return api.delete('/cart');
    }
};

export const userApi = {
    async getProfile() {
        return api.get('/user/profile');
    },

    async updateProfile(data) {
        return api.put('/user/profile', data);
    },

    async getOrders() {
        return api.get('/user/orders');
    },

    async getWishlist() {
        return api.get('/user/wishlist');
    },

    async addToWishlist(dealId) {
        return api.post('/user/wishlist', { dealId });
    },

    async removeFromWishlist(dealId) {
        return api.delete(`/user/wishlist/${dealId}`);
    }
};

export const reviewsApi = {
    async getForDeal(dealId, page = 1, limit = 10) {
        return api.get(`/reviews/deal/${dealId}?page=${page}&limit=${limit}`);
    },

    async create(dealId, data) {
        return api.post(`/reviews/deal/${dealId}`, data);
    }
};

// Export default API client
export default api;
