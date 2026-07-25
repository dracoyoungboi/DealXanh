/**
 * Test script - Copy this to home.js if products don't show up
 */

console.log('=== DealXanh Home Loading ===');

// Simple render function
function renderDeals() {
    const featuredGrid = document.getElementById('featured-deals');
    const blindboxGrid = document.getElementById('blindbox-deals');
    const nearYouGrid = document.getElementById('near-you-deals');

    console.log('Containers found:', {
        featured: !!featuredGrid,
        blindbox: !!blindboxGrid,
        nearYou: !!nearYouGrid
    });

    if (!featuredGrid) {
        console.error('featured-deals container not found!');
        return;
    }

    // Sample deal data
    const deals = [
        {
            id: '1',
            title: 'Bánh mì pate nóng hổi - Giảm 50%',
            storeName: 'Bánh mì Hòa Mai',
            storeAvatar: 'https://images.unsplash.com/photo-1585109649139-366815a0d713?w=48&h=48&fit=crop&q=80',
            originalPrice: 25000,
            price: 12500,
            discount: 50,
            expiryDate: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1586305398049-70064eee4e6f?w=400&h=300&fit=crop&q=80',
            distance: 350,
            availableSlots: 5
        },
        {
            id: '2',
            title: 'Combo bún bò Huế - Giảm 40%',
            storeName: 'Bún bò Thảo',
            storeAvatar: 'https://images.unsplash.com/photo-1512058564366-18510be2db19?w=48&h=48&fit=crop&q=80',
            originalPrice: 45000,
            price: 27000,
            discount: 40,
            expiryDate: new Date(Date.now() + 1 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=400&h=300&fit=crop&q=80',
            distance: 520,
            availableSlots: 3
        },
        {
            id: '3',
            title: 'Cơm tấm sườn nướng - Giảm 45%',
            storeName: 'Cơm Niêu Nhà',
            storeAvatar: 'https://images.unsplash.com/photo-1512058564366-18510be2db19?w=48&h=48&fit=crop&q=80',
            originalPrice: 55000,
            price: 30000,
            discount: 45,
            expiryDate: new Date(Date.now() + 3 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=400&h=300&fit=crop&q=80',
            distance: 480,
            availableSlots: 8
        },
        {
            id: '4',
            title: 'Trà sữa trân châu cận date',
            storeName: 'Mixue Hòa Lạc',
            storeAvatar: 'https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=48&h=48&fit=crop&q=80',
            originalPrice: 30000,
            price: 18000,
            discount: 40,
            expiryDate: new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1558160074-4d7d8bdf4256?w=400&h=300&fit=crop&q=80',
            distance: 280,
            availableSlots: 12
        },
        {
            id: '5',
            title: 'Bánh croissant bơ Pháp - Giảm 50%',
            storeName: 'La Boulangerie',
            storeAvatar: 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=48&h=48&fit=crop&q=80',
            originalPrice: 35000,
            price: 17500,
            discount: 50,
            expiryDate: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1555507036-ab1f40388085?w=400&h=300&fit=crop&q=80',
            distance: 450,
            availableSlots: 6
        },
        {
            id: '6',
            title: 'Khoai tây chiên - Giảm 50%',
            storeName: 'Potato Stories',
            storeAvatar: 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=48&h=48&fit=crop&q=80',
            originalPrice: 35000,
            price: 17500,
            discount: 50,
            expiryDate: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
            image: 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400&h=300&fit=crop&q=80',
            distance: 310,
            availableSlots: 12
        }
    ];

    // Render deals
    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            minimumFractionDigits: 0
        }).format(amount);
    };

    const formatTime = (date) => {
        const now = new Date();
        const target = new Date(date);
        const diff = target - now;
        const hours = Math.floor(diff / 3600000);
        if (hours < 1) return 'Gấp!';
        if (hours < 24) return `${hours} giờ nữa`;
        return 'Hôm nay';
    };

    const createCard = (deal) => `
        <div class="deal-card" data-deal-id="${deal.id}">
            <div class="deal-card__image">
                <img src="${deal.image}" alt="${deal.title}" loading="lazy">
                <div class="deal-card__badges">
                    <span class="deal-card__badge">-${deal.discount}%</span>
                </div>
                <div class="deal-card__expiry">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                    ${formatTime(deal.expiryDate)}
                </div>
            </div>
            <div class="deal-card__content">
                <div class="deal-card__store">
                    <img src="${deal.storeAvatar}" alt="${deal.storeName}" class="deal-card__store-avatar">
                    <span class="deal-card__store-name">${deal.storeName}</span>
                </div>
                <h3 class="deal-card__title">${deal.title}</h3>
                <div class="deal-card__price">
                    <span class="deal-card__current">${formatCurrency(deal.price)}</span>
                    <span class="deal-card__original">${formatCurrency(deal.originalPrice)}</span>
                </div>
                <div class="deal-card__meta">
                    <span class="deal-card__distance">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                            <circle cx="12" cy="10" r="3"></circle>
                        </svg>
                        ${(deal.distance / 1000).toFixed(1)}km
                    </span>
                    <span class="deal-card__slots">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                            <circle cx="12" cy="7" r="4"></circle>
                        </svg>
                        Còn ${deal.availableSlots} slot
                    </span>
                </div>
            </div>
        </div>
    `;

    featuredGrid.innerHTML = deals.map(createCard).join('');
    console.log('Rendered', deals.length, 'deals to featured grid');

    if (blindboxGrid) {
        blindboxGrid.innerHTML = deals.slice(0, 3).map(createCard).join('');
        console.log('Rendered blind box deals');
    }

    if (nearYouGrid) {
        nearYouGrid.innerHTML = deals.slice(0, 4).map(createCard).join('');
        console.log('Rendered near you deals');
    }
}

// Initialize
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', renderDeals);
} else {
    renderDeals();
}
