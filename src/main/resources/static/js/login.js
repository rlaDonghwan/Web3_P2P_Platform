const loginButton = document.getElementById('loginButton');
const loginModal = document.getElementById('loginModal');
const balanceDisplay = document.getElementById('balanceDisplay');

loginButton.addEventListener('click', () => {
    loginModal.style.display = 'flex';
});

function closeModal() {
    loginModal.style.display = 'none';
}

let isProcessingRequest = false; // 요청 중인지 확인하는 변수

async function connectMetaMask() {
    if (window.ethereum) {
        if (isProcessingRequest) {
            alert('이미 지갑 연결 요청을 처리 중입니다. 잠시만 기다려주세요.');
            return;
        }
        try {
            isProcessingRequest = true; // 요청 시작
            const accounts = await ethereum.request({ method: 'eth_requestAccounts' });
            const account = accounts[0];

            console.log("Connected account:", account);

            // Web3 초기화 및 잔액 가져오기
            const web3 = new Web3(window.ethereum);
            const balance = await web3.eth.getBalance(account);
            const etherBalance = web3.utils.fromWei(balance, 'ether');

            // 잔액 표시
            balanceDisplay.style.display = 'block';
            balanceDisplay.innerText = `ETH 잔액: ${etherBalance} ETH`;

            // 로그인 버튼 숨기기
            loginButton.style.display = 'none';

            // Nonce 가져오기
            const nonce = await getNonce(account);
            console.log("Nonce received:", nonce);

            const message = `Signing a message to login: ${nonce}`;
            console.log("Message to sign:", message);

            const signature = await window.ethereum.request({
                method: 'personal_sign',
                params: [message, account]
            });
            console.log("Signature:", signature);

            // JSON 데이터 전송
            const response = await fetch('/auth/login-with-signature', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ account, signature })
            });

            // 응답 확인
            if (response.ok) {
                alert('로그인 성공!');
                window.location.href = '/home';
            } else {
                const errorText = await response.text();
                alert('로그인 실패: ' + errorText);
            }

            // MetaMask 연결 완료 후 모달 창 닫기
            closeModal();

        } catch (error) {
            console.error('MetaMask 연결 실패:', error);
        } finally {
            isProcessingRequest = false;
        }
    } else {
        alert('MetaMask가 설치되어 있지 않습니다.');
    }
}

async function getNonce(address) {
    try {
        const response = await fetch(`/nonce/${address}`);
        if (response.ok) {
            return await response.text();
        } else {
            console.error("Nonce 요청 실패");
            throw new Error("Nonce 요청 실패");
        }
    } catch (error) {
        console.error("Nonce 요청 실패:", error);
        throw error;
    }
}