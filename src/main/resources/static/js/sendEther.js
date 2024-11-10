let useMetaMask = true;

async function connectMetaMask() {
    if (typeof window.ethereum !== 'undefined') {
        try {
            const accounts = await ethereum.request({ method: 'eth_requestAccounts' });
            document.getElementById('fromAddress').value = accounts[0];
            document.getElementById('status').innerHTML = "MetaMask connected!";
        } catch (error) {
            document.getElementById('status').innerHTML = `Error: ${error.message}`;
            document.getElementById('status').classList.add('error');
        }
    } else {
        alert("MetaMask not installed");
    }
}

async function sendEther() {
    const fromAddress = document.getElementById("fromAddress").value;
    const toAddress = document.getElementById("toAddress").value;
    const amount = document.getElementById("amount").value;

    if (!fromAddress || !toAddress || !amount) {
        document.getElementById("status").innerHTML = "Fill in all fields";
        document.getElementById('status').classList.add('error');
        return;
    }

    if (useMetaMask && typeof window.ethereum !== 'undefined') {
        try {
            const web3 = new Web3(window.ethereum);
            const gasPrice = await web3.eth.getGasPrice();  // 네트워크에서 동적 가스 가격 가져오기
            const estimatedGas = await web3.eth.estimateGas({
                to: toAddress,
                from: fromAddress,
                value: web3.utils.toHex(web3.utils.toWei(amount, 'ether')),
            });

            const transactionParameters = {
                to: toAddress,
                from: fromAddress,
                value: web3.utils.toHex(web3.utils.toWei(amount, 'ether')),
                gas: web3.utils.toHex(estimatedGas), // 추정된 가스 한도 사용
                gasPrice: web3.utils.toHex(gasPrice),  // 네트워크에서 받은 가스 가격
            };

            ethereum
                .request({
                    method: 'eth_sendTransaction',
                    params: [transactionParameters],
                })
                .then((txHash) => {
                    document.getElementById("status").innerHTML = `Transaction Hash: ${txHash}`;
                    document.getElementById('status').classList.remove('error');
                })
                .catch((error) => {
                    console.log(error); // 전체 에러 객체 출력
                    document.getElementById("status").innerHTML = `Error: ${error.message}`;
                    document.getElementById('status').classList.add('error');
                });
        } catch (error) {
            console.log(error);  // 전체 에러 객체 출력
            document.getElementById("status").innerHTML = `Error: ${error.message}`;
            document.getElementById('status').classList.add('error');
        }
    } else {
        document.getElementById("status").innerHTML = "MetaMask is not installed.";
        document.getElementById('status').classList.add('error');
    }
}