const loginButton = document.getElementById('loginButton');
const loginModal = document.getElementById('loginModal');
const balanceDisplay = document.getElementById('balanceDisplay');

loginButton.addEventListener('click', () => {
    loginModal.style.display = 'flex';
});

function closeModal() {
    loginModal.style.display = 'none';
}

let isProcessingRequest = false;  // 요청 중인지 확인하는 변수

async function connectMetaMask() {
    if (window.ethereum) {
        if (isProcessingRequest) {
            alert('이미 지갑 연결 요청을 처리 중입니다. 잠시만 기다려주세요.');
            return;  // 이미 요청 처리 중이면 새로운 요청을 보내지 않음
        }

        try {
            isProcessingRequest = true;  // 요청 시작
            await ethereum.request({ method: 'eth_requestAccounts' });
            const web3 = new Web3(window.ethereum);  // Web3 초기화
            const accounts = await web3.eth.getAccounts();
            const account = accounts[0];
            alert('MetaMask 연결 성공! 주소: ' + account);

            // 잔액 가져오기
            const balance = await web3.eth.getBalance(account);
            const etherBalance = web3.utils.fromWei(balance, 'ether');

            // 잔액 표시
            balanceDisplay.style.display = 'block';
            balanceDisplay.innerText = `ETH 잔액: ${etherBalance} ETH`;

            // 로그인 버튼 숨기고 잔액 표시
            loginButton.style.display = 'none';

            const message = '로그인을 위한 서명';
            const signature = await web3.eth.personal.sign(message, account, "");  // 세 번째 인자로 빈 문자열 추가
            const response = await fetch('/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ account, signature })
            });

            if (response.ok) {
                alert('로그인 성공!');
                window.location.href = '/home';
            } else {
                alert('로그인 실패');
            }

            // MetaMask 연결이 완료되면 모달 창 닫기
            closeModal();  // 모달 창 닫기 함수 호출

        } catch (error) {
            console.error('MetaMask 연결 실패:', error);
        } finally {
            isProcessingRequest = false;  // 요청 완료 후 플래그 초기화
        }
    } else {
        alert('MetaMask가 설치되어 있지 않습니다.');
    }
}