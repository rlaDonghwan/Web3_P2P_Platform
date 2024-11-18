// 슬라이드 기능 초기화
let currentSlideIndex = 0;
const slides = document.querySelectorAll('.product-detail-image img');

function showSlide(index) {
    slides.forEach((slide, i) => {
        slide.style.display = i === index ? 'block' : 'none';
    });
}

function changeSlide(direction) {
    currentSlideIndex += direction;
    if (currentSlideIndex < 0) currentSlideIndex = slides.length - 1;
    else if (currentSlideIndex >= slides.length) currentSlideIndex = 0;
    showSlide(currentSlideIndex);
}

showSlide(currentSlideIndex); // 초기 슬라이드 표시

// 장바구니 추가
function addToCart(itemId) {
    fetch(`/cart/add?itemId=${itemId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
    })
        .then(response => {
            if (response.status === 401) {
                alert("로그인이 필요합니다. 홈 화면으로 이동합니다.");
                window.location.href = "/home";
                throw new Error("로그인이 필요합니다.");
            }
            if (!response.ok) throw new Error("장바구니 추가 실패");
            return response.text();
        })
        .then(message => {
            alert(message);
            if (confirm("장바구니에 담았습니다. 장바구니로 이동하시겠습니까?")) {
                window.location.href = "/cart";
            }
        })
        .catch(error => console.error('장바구니 추가 중 오류:', error));
}

// 상품 수정 페이지로 이동
function editProduct() {
    const itemId = [[${item.itemId}]];
    window.location.href = `/editItem/${itemId}`;
}

function payProduct() {
    const itemId = [[${item.itemId}]];
    window.location.href = `/pay/${itemId}`;
}



// 상품 삭제
function deleteProduct() {
    const itemId = [[${item.itemId}]];
    if (confirm("정말로 이 상품을 삭제하시겠습니까?")) {
        fetch(`/delete/${itemId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })
            .then(response => {
                if (!response.ok) throw new Error("상품 삭제 실패");
                alert("상품이 삭제되었습니다.");
                window.location.href = "/home";
            })
            .catch(error => {
                console.error('상품 삭제 중 오류:', error);
                alert("상품 삭제 중 오류가 발생했습니다.");
            });
    }
}

// 드롭다운 메뉴 토글
function toggleDropdownMenu() {
    const menu = document.getElementById('dropdownMenu');
    menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
}

// 메뉴 외부 클릭 시 드롭다운 닫기
document.addEventListener('click', event => {
    const dropdown = document.getElementById('dropdownMenu');
    const actionIcon = document.querySelector('.action-icon');
    if (!actionIcon.contains(event.target) && dropdown.style.display === 'block') {
        dropdown.style.display = 'none';
    }
});