./gradlew imageCheck:assembleDebug
./gradlew imageCheck:publishPluginPublicationToNexusRepository
p=`pwd`
cd /Users/liuhuiliang/work/gitMaven
echo ".DS_Store" > .gitignore
git add .
git commit -m 这是测试
git push origin master
cd $p

