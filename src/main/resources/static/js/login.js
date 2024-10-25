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
            console.log("Account address:", account);
            alert('MetaMask 연결 성공! 주소: ' + account);

            // 잔액 가져오기
            const balance = await web3.eth.getBalance(account);
            const etherBalance = web3.utils.fromWei(balance, 'ether');
            // console.log("Ether Balance:", etherBalance);

            // 잔액 표시
            balanceDisplay.style.display = 'block';
            balanceDisplay.innerText = `ETH 잔액: ${etherBalance} ETH`;

            // 로그인 버튼 숨기고 잔액 표시
            loginButton.style.display = 'none';

            const message = '로그인을 위한 서명';
            const signature = await web3.eth.personal.sign(message, account, "");  // 세 번째 인자로 빈 문자열 추가
            console.log("Signature to be sent:", signature);

            // JSON 데이터 전송
            const response = await fetch('/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ account, signature })
            });

            console.log("JSON being sent:", JSON.stringify({ account, signature }));
            console.log("Response status:", response.status);

// 응답 확인
            if (response.ok) {
                alert('로그인 성공!');
                window.location.href = '/home';
            } else {
                const errorText = await response.text(); // 서버에서 반환된 에러 메시지
                console.error('Error:', errorText);
                alert('로그인 실패: ' + errorText); // 사용자에게 에러 메시지 표시
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