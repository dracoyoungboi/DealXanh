/**
 * Home Page JavaScript - DealXanh
 * Simple version to ensure products display
 */

console.log('=== DealXanh Home Loading ===');

// Sample deals data with real images
const sampleDeals = [
    {
        id: '1',
        title: 'Bánh mì pate nóng hổi - Giảm 50%',
        storeName: 'Bánh mì Hòa Mai',
        storeAvatar: 'https://images.unsplash.com/photo-1585109649139-366815a0d713?w=48&h=48&fit=crop&q=80',
        originalPrice: 25000,
        price: 12500,
        discount: 50,
        expiryDate: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        image: 'https://thanhnien.mediacdn.vn/Uploaded/trantam/2022_11_17/anh-chup-man-hinh-2022-10-07-luc-172838-4541-9511.png',
        category: 'food',
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
        category: 'food',
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
        category: 'food',
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
        category: 'beverage',
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
        image: 'https://vcdn1-dulich.vnecdn.net/2022/01/06/230702205-374170334060215-7790-7309-5717-1641455161.jpg?w=680&h=0&q=100&dpr=2&fit=crop&s=5ntmA3l7AjA1rc5NWH-BGg',
        category: 'bakery',
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
        category: 'snack',
        distance: 310,
        availableSlots: 12
    },
    {
        id: '7',
        title: 'Phở bò tái chấm - Giảm 40%',
        storeName: 'Phở Gia Truyền',
        storeAvatar: 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=48&h=48&fit=crop&q=80',
        originalPrice: 50000,
        price: 30000,
        discount: 40,
        expiryDate: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        image: 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=400&h=300&fit=crop&q=80',
        category: 'food',
        distance: 380,
        availableSlots: 7
    },
    {
        id: '8',
        title: 'Cà phê sữa đá cận date',
        storeName: 'Cà phê Cầu Tre',
        storeAvatar: 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=48&h=48&fit=crop&q=80',
        originalPrice: 25000,
        price: 15000,
        discount: 40,
        expiryDate: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        image: 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&q=80',
        category: 'beverage',
        distance: 320,
        availableSlots: 15
    }
];

// Utility functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(amount);
}

function formatTime(date) {
    const now = new Date();
    const target = new Date(date);
    const diff = target - now;
    const hours = Math.floor(diff / 3600000);
    if (hours < 1) return 'Gấp!';
    if (hours < 24) return hours + ' giờ nữa';
    return 'Hôm nay';
}

function createDealCard(deal) {
    return '<div class="deal-card" data-deal-id="' + deal.id + '">' +
        '<div class="deal-card__image">' +
        '<img src="' + deal.image + '" alt="' + deal.title + '" loading="lazy">' +
        '<div class="deal-card__badges">' +
        '<span class="deal-card__badge">-' + deal.discount + '%</span>' +
        '</div>' +
        '<div class="deal-card__expiry">' +
        '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">' +
        '<circle cx="12" cy="12" r="10"></circle>' +
        '<polyline points="12 6 12 12 16 14"></polyline>' +
        '</svg>' +
        formatTime(deal.expiryDate) +
        '</div>' +
        '</div>' +
        '<div class="deal-card__content">' +
        '<div class="deal-card__store">' +
        '<img src="' + deal.storeAvatar + '" alt="' + deal.storeName + '" class="deal-card__store-avatar">' +
        '<span class="deal-card__store-name">' + deal.storeName + '</span>' +
        '</div>' +
        '<h3 class="deal-card__title">' + deal.title + '</h3>' +
        '<div class="deal-card__price">' +
        '<span class="deal-card__current">' + formatCurrency(deal.price) + '</span>' +
        '<span class="deal-card__original">' + formatCurrency(deal.originalPrice) + '</span>' +
        '</div>' +
        '<div class="deal-card__meta">' +
        '<span class="deal-card__distance">' +
        '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">' +
        '<path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>' +
        '<circle cx="12" cy="10" r="3"></circle>' +
        '</svg>' +
        (deal.distance / 1000).toFixed(1) + 'km' +
        '</span>' +
        '<span class="deal-card__slots">' +
        '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">' +
        '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>' +
        '<circle cx="12" cy="7" r="4"></circle>' +
        '</svg>' +
        'Còn ' + deal.availableSlots + ' slot' +
        '</span>' +
        '</div>' +
        '</div>' +
        '</div>';
}

function renderAllDeals() {
    console.log('renderAllDeals() called');

    // Get containers
    var featuredGrid = document.getElementById('featured-deals');
    var blindboxGrid = document.getElementById('blindbox-deals');
    var nearYouGrid = document.getElementById('near-you-deals');

    console.log('Containers:', {
        featured: featuredGrid !== null,
        blindbox: blindboxGrid !== null,
        nearYou: nearYouGrid !== null
    });

    if (!featuredGrid) {
        console.error('featured-deals container NOT FOUND!');
        return;
    }

    // Render featured deals
    var html = '';
    for (var i = 0; i < sampleDeals.length; i++) {
        html += createDealCard(sampleDeals[i]);
    }
    featuredGrid.innerHTML = html;
    console.log('Rendered ' + sampleDeals.length + ' deals to featured grid');

    // Render blind box deals (use first 3)
    if (blindboxGrid) {
        var blindboxHtml = '';
        for (var j = 0; j < 3 && j < sampleDeals.length; j++) {
            blindboxHtml += createDealCard(sampleDeals[j]);
        }
        blindboxGrid.innerHTML = blindboxHtml;
        console.log('Rendered blind box deals');
    }

    // Render near you deals (use first 4)
    if (nearYouGrid) {
        var nearYouHtml = '';
        for (var k = 0; k < 4 && k < sampleDeals.length; k++) {
            nearYouHtml += createDealCard(sampleDeals[k]);
        }
        nearYouGrid.innerHTML = nearYouHtml;
        console.log('Rendered near you deals');
    }
}

// Start flash sale timer
function startFlashSaleTimer() {
    var endTime = new Date(Date.now() + 2 * 60 * 60 * 1000 + 30 * 60 * 1000);

    function updateTimer() {
        var now = new Date();
        var diff = endTime - now;

        if (diff <= 0) return;

        var hours = Math.floor(diff / 3600000);
        var minutes = Math.floor((diff % 3600000) / 60000);
        var seconds = Math.floor((diff % 60000) / 1000);

        var hoursEl = document.getElementById('timer-hours');
        var minutesEl = document.getElementById('timer-minutes');
        var secondsEl = document.getElementById('timer-seconds');

        if (hoursEl) hoursEl.textContent = String(hours).padStart(2, '0');
        if (minutesEl) minutesEl.textContent = String(minutes).padStart(2, '0');
        if (secondsEl) secondsEl.textContent = String(seconds).padStart(2, '0');
    }

    updateTimer();
    setInterval(updateTimer, 1000);
}

// Filter by category
function filterByCategory(category) {
    var filtered = sampleDeals;
    if (category !== 'all') {
        filtered = sampleDeals.filter(function(deal) {
            return deal.category === category;
        });
    }

    var featuredGrid = document.getElementById('featured-deals');
    if (featuredGrid) {
        var html = '';
        for (var i = 0; i < filtered.length; i++) {
            html += createDealCard(filtered[i]);
        }
        featuredGrid.innerHTML = html;
    }
}

// Setup event listeners
function setupEvents() {
    // Category filter chips
    var filterChips = document.querySelectorAll('.filter-chip');
    filterChips.forEach(function(chip) {
        chip.addEventListener('click', function() {
            var category = chip.dataset.category;
            filterByCategory(category);

            // Update active state
            filterChips.forEach(function(c) {
                c.classList.remove('filter-chip--active');
            });
            chip.classList.add('filter-chip--active');
        });
    });

    // Category cards
    var categoryCards = document.querySelectorAll('.category-card');
    categoryCards.forEach(function(card) {
        card.addEventListener('click', function() {
            var category = card.dataset.category;
            filterByCategory(category);
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    });

    // Blind box CTA
    var blindBoxCta = document.querySelector('.blind-box-section__cta');
    if (blindBoxCta) {
        blindBoxCta.addEventListener('click', function() {
            filterByCategory('blindbox');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // Flash sale button
    var flashSaleBtn = document.querySelector('.flash-sale__button');
    if (flashSaleBtn) {
        flashSaleBtn.addEventListener('click', function() {
            window.scrollTo({ top: 400, behavior: 'smooth' });
        });
    }

    console.log('Events setup complete');
}

// Initialize
function init() {
    console.log('Init called');
    renderAllDeals();
    startFlashSaleTimer();
    setupEvents();
    console.log('Init complete!');
}

// Wait for DOM
if (document.readyState === 'loading') {
    console.log('Waiting for DOM...');
    document.addEventListener('DOMContentLoaded', init);
} else {
    console.log('DOM ready');
    init();
}
