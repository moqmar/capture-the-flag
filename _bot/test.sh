cd "$(dirname "$0")"

amount=$1
if [ "$amount" == "" ]; then amount=5; fi
#active=$2
#if [ "$active" == "" ]; then active=2; fi
#inactive=$((amount-active))
inactive=$amount

for i in $(seq 1 $inactive); do (node . mobot-$i &); done
wait $pid
#for i in $(seq 1 $active); do (node . mobot-$i  &); done
