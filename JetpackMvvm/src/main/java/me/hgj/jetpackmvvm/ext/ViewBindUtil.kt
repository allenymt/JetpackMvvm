package me.hgj.jetpackmvvm.ext

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

/**
 * 作者　: hegaojian
 * 时间　: 2021/12/21
 * 描述　:
 */

@JvmName("inflateWithGeneric")
fun <VB : ViewBinding> AppCompatActivity.inflateBindingWithGeneric(layoutInflater: LayoutInflater): VB =
    withGenericBindingClass<VB>(this) { clazz ->
        //找到ViewBinding后，通过反射获得注入
        clazz.getMethod("inflate", LayoutInflater::class.java).invoke(null, layoutInflater) as VB
    }.also { binding ->
        if (binding is ViewDataBinding) {
            binding.lifecycleOwner = this
        }
    }

@JvmName("inflateWithGeneric")
fun <VB : ViewBinding> Fragment.inflateBindingWithGeneric(
    layoutInflater: LayoutInflater,
    parent: ViewGroup?,
    attachToParent: Boolean
): VB =
    withGenericBindingClass<VB>(this) { clazz ->
        clazz.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
            .invoke(null, layoutInflater, parent, attachToParent) as VB
    }.also { binding ->
        if (binding is ViewDataBinding) {
            binding.lifecycleOwner = viewLifecycleOwner
        }
    }

/**
 *
 * @param any Any 入参，可以是Activity、Fragment、View、ViewGroup等，通过查找这个类的泛型，获取到ViewBinding的类型
 * @param block Function1<Class<VB>, VB>
 * @return VB
 */
private fun <VB : ViewBinding> withGenericBindingClass(any: Any, block: (Class<VB>) -> VB): VB {
    var genericSuperclass = any.javaClass.genericSuperclass
    var superclass = any.javaClass.superclass
    while (superclass != null) {
        if (genericSuperclass is ParameterizedType) {
            try {
                android.util.Log.e(
                    "withGenericBindingClass",
                    "genericSuperclass is $genericSuperclass length is ${genericSuperclass.actualTypeArguments.size}"
                )
                // 这里默认取下标为1的值，是有风险的，这会要求VB的泛型声明必须在第二个位置，如果不是，则会抛出异常
                return block.invoke(genericSuperclass.actualTypeArguments[1] as Class<VB>)
            } catch (e: NoSuchMethodException) {
            } catch (e: ClassCastException) {
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        }
        genericSuperclass = superclass.genericSuperclass
        superclass = superclass.superclass
    }
    throw IllegalArgumentException("There is no generic of ViewBinding.")
}